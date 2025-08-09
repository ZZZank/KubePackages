package zank.mods.kube_packages.bridge;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
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
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(
            VersionRange.class,
            from -> from instanceof CharSequence,
            (from) -> {
                try {
                    return VersionRange.createFromVersionSpec(from.toString());
                } catch (InvalidVersionSpecificationException e) {
                    throw new RuntimeException(e);
                }
            }
        );
        typeWrappers.registerSimple(
            ArtifactVersion.class,
            from -> from instanceof CharSequence,
            (from) -> new DefaultArtifactVersion(from.toString())
        );
    }

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
        KubePackages.registerProvider(new DirKubePackageProvider(KubePackagePaths.CUSTOM));
        // zip
        KubePackages.registerProvider(new ZipKubePackageProvider(KubePackagePaths.CUSTOM));
        //kubejs dummy, for sorting content packs
        KubePackages.registerProvider(
            new DummyKubePackageProvider(List.of(new DummyKubePackage(KubeJS.MOD_ID, cx -> null)))
        );
        // mod
        ModList.get()
            .getModFiles()
            .stream()
            .filter(ModKubePackageProvider::validate)
            .map(ModKubePackageProvider::new)
            .forEach(KubePackages::registerProvider);
    }

    @Override
    public void clearCaches() {
        KubePackages.clearPackages();
    }
}
