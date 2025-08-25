package zank.mods.kube_packages.bridge.mixin;

import org.slf4j.event.Level;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.inject.ScriptPackLoadHelper;
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


@Mixin(value = ScriptManager.class, remap = false)
public abstract class MixinScriptManager implements ScriptPackLoadHelper {

    @Shadow
    protected abstract void loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source);

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
    private Collection<ScriptPack> injectPacks(Map<String, ScriptPack> original) {
        var context = new ScriptLoadContext((ScriptManager) (Object) this);
        var console = context.console();

        var packages = KubePackages.getPackages();

        var report = KubePackages.getPackageLoadReport();

        console.info(String.format(
            "Found %s packages with %s error(s), %s warning(s) and %s info(s): %s",
            packages.size(),
            report.getReportsAt(Level.ERROR).size(),
            report.getReportsAt(Level.WARN).size(),
            report.getReportsAt(Level.INFO).size(),
            packages.values()
        ));
        report.forEach((level, text) -> {
            switch (level) {
                case ERROR -> console.error(text.getString());
                case WARN -> console.warn(text.getString());
                case INFO -> console.info(text.getString());
            }
        });

        if (!report.getReportsAt(Level.ERROR).isEmpty()) {
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

            sortablePacks.put(namespace, new SortableKubePackage(namespace, pkg, scriptPacks));
        }

        var dependencyBuilder = new PackDependencyBuilder();
        dependencyBuilder.build(sortablePacks.values());

        try {
            return TopoSort.sort(sortablePacks.values())
                .stream()
                .map(SortableKubePackage::scriptPacks)
                .flatMap(Collection::stream)
                .toList();
        } catch (TopoNotSolved | TopoPreconditionFailed e) {
            console.error("Unable to sort loaded packages, ignoring all installed packages", e);
            return original.values();
        }
    }

    @Override
    public void kpkg$loadFile(ScriptPack pack, ScriptFileInfo fileInfo, ScriptSource source) {
        this.loadFile(pack, fileInfo, source);
    }
}
