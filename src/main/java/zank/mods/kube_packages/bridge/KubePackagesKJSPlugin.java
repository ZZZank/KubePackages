package zank.mods.kube_packages.bridge;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import net.minecraftforge.fml.ModList;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.inject.SortablePackageHolder;
import zank.mods.kube_packages.impl.dummy.DummyKubePackage;
import zank.mods.kube_packages.impl.dummy.DummyKubePackageProvider;
import zank.mods.kube_packages.impl.mod.ModKubePackageProvider;
import zank.mods.kube_packages.impl.path.DirKubePackageProvider;
import zank.mods.kube_packages.impl.zip.ZipKubePackageProvider;

import java.util.List;

/**
 * @author ZZZank
 */
public class KubePackagesKJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add(
            "KubePackages",
            new KubePackagesBinding(
                event.getType(),
                (SortablePackageHolder) event.manager
            )
        );
    }

    @Override
    public void init() {
        //path
        zank.mods.kube_packages.KubePackages.registerProvider(new DirKubePackageProvider(KubePackagePaths.CUSTOM));
        // zip
        zank.mods.kube_packages.KubePackages.registerProvider(new ZipKubePackageProvider(KubePackagePaths.CUSTOM));
        //kubejs dummy, for sorting content packs
        zank.mods.kube_packages.KubePackages.registerProvider(
            new DummyKubePackageProvider(List.of(new DummyKubePackage(KubeJS.MOD_ID, cx -> null)))
        );
        // mod
        ModList.get()
            .getModFiles()
            .stream()
            .filter(ModKubePackageProvider::validate)
            .map(ModKubePackageProvider::new)
            .forEach(zank.mods.kube_packages.KubePackages::registerProvider);
    }

    @Override
    public void clearCaches() {
        KubePackages.clearPackages();
    }
}
