package zank.mods.kube_packages.bridge.kubejs;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.inject.SortablePackageHolder;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.bridge.KubePackagePaths;
import zank.mods.kube_packages.impl.dummy.DummyKubePackage;
import zank.mods.kube_packages.impl.dummy.DummyKubePackageProvider;
import zank.mods.kube_packages.impl.mod.ModKubePackageProvider;
import zank.mods.kube_packages.impl.path.DirKubePackageProvider;
import zank.mods.kube_packages.impl.zip.ZipKubePackageProvider;
import zank.mods.kube_packages.utils.CodecUtil;

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
            CodecUtil.wrapUnsafeFn(
                from -> VersionRange.createFromVersionSpec(from.toString())
            )::applyOrThrow
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
        KubePackages.registerProvider(new DirKubePackageProvider(KubePackagePaths.PACKAGES));
        // zip
        KubePackages.registerProvider(new ZipKubePackageProvider(KubePackagePaths.PACKAGES));
        // mod
        KubePackages.registerProvider(new ModKubePackageProvider());
        //kubejs dummy, for sorting packages
        KubePackages.registerProvider(
            new DummyKubePackageProvider(List.of(new DummyKubePackage(
                PackageMetaData.minimal(
                    KubeJS.MOD_ID,
                    new DefaultArtifactVersion("1.1.1")
                ), cx -> null
            )))
        );
    }

    @Override
    public void clearCaches() {
        KubePackages.clearPackages();
    }
}
