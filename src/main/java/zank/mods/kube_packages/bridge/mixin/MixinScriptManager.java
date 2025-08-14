package zank.mods.kube_packages.bridge.mixin;

import net.minecraft.network.chat.Component;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.KubePackagesConfig;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.inject.ScriptPackLoadHelper;
import zank.mods.kube_packages.api.inject.SortablePackageHolder;
import zank.mods.kube_packages.impl.dependency.PackDependencyBuilder;
import zank.mods.kube_packages.impl.dependency.PackDependencyValidator;
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


@Mixin(value = ScriptManager.class, remap = false)
public abstract class MixinScriptManager implements SortablePackageHolder, ScriptPackLoadHelper {

    @Shadow
    protected abstract void loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);

    @Unique
    private Map<String, SortableKubePackage> kpkg$sortables;

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<ScriptPack> injectPacks(Map<String, ScriptPack> original) {
        var context = new ScriptLoadContext((ScriptManager) (Object) this);
        var packages = KubePackages.getPackages();

        var report = new PackDependencyValidator(KubePackagesConfig.DUPE_HANDLING.get())
            .validate(packages);
        report.infos().stream().map(Component::getString).forEach(context.console()::info);
        report.warnings().stream().map(Component::getString).forEach(context.console()::warn);
        report.errors().stream().map(Component::getString).forEach(context.console()::error);
        if (!report.errors().isEmpty()) {
            return original.values();
        }

        var sortablePacks = new HashMap<String, SortableKubePackage>();

        for (var pkg : packages) {
            KubePackages.LOGGER.debug("Found package: {}", pkg);
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
            context.console().error("Unable to sort packages, ignoring all installed packages", e);
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
