package zank.mods.kube_packages.bridge.mixin;

import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.inject.ScriptPackLoadHelper;
import zank.mods.kube_packages.api.inject.SortablePackageHolder;
import zank.mods.kube_packages.impl.dependency.PackDependencyBuilder;
import zank.mods.kube_packages.impl.dependency.SortableKubePackage;
import zank.mods.kube_packages.utils.topo.TopoNotSolved;
import zank.mods.kube_packages.utils.topo.TopoPreconditionFailed;
import zank.mods.kube_packages.utils.topo.TopoSort;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Mixin(value = ScriptManager.class, remap = false)
public abstract class MixinScriptManager implements SortablePackageHolder, ScriptPackLoadHelper {

    @Shadow
    protected abstract void loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);

    @Unique
    private Map<String, SortableKubePackage> kpkg$sortables;

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<ScriptPack> injectPacks(Map<String, ScriptPack> original) {
        var context = new ScriptLoadContext((ScriptManager) (Object) this);
        var console = context.console();

        var hasError = new AtomicBoolean(false);
        var packages = KubePackages.getPackages((level, text) -> {
            switch (level) {
                case ERROR -> {
                    hasError.set(true);
                    console.error(text.getString());
                }
                case WARN -> console.warn(text.getString());
                case INFO -> console.info(text.getString());
            }
        });

        if (hasError.get()) {
            console.error("KubePackages found error when loading packages, ignoring all installed packages");
            return original.values();
        }

        var sortablePacks = new HashMap<String, SortableKubePackage>();

        for (var pkg : packages.values()) {
            var scriptPack = pkg.getScript(context);
            var namespace = pkg.id();

            List<ScriptPack> scriptPacks;
            if (KubeJS.MOD_ID.equals(namespace)) {
                scriptPacks = List.copyOf(original.values());
            } else if (scriptPack != null) {
                scriptPacks = List.of(pkg.postProcess(context, scriptPack));
            } else {
                scriptPacks = List.of();
            }

            var sortable = new SortableKubePackage(
                namespace,
                pkg,
                scriptPacks
            );
            sortablePacks.put(namespace, sortable);
        }

        kpkg$sortables = Map.copyOf(sortablePacks);

        var dependencyBuilder = new PackDependencyBuilder();
        dependencyBuilder.build(sortablePacks.values());

        try {
            return TopoSort.sort(sortablePacks.values())
                .stream()
                .map(SortableKubePackage::scriptPacks)
                .flatMap(Collection::stream)
                .toList();
        } catch (TopoNotSolved | TopoPreconditionFailed e) {
            console.error("Unable to sort packages, ignoring all installed packages", e);
            return original.values();
        }
    }

    @Override
    public Map<String, SortableKubePackage> kpkg$sortablePacks() {
        return kpkg$sortables;
    }

    @Override
    public void kpkg$loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source) {
        this.loadFile(pack, fileInfo, source);
    }
}
