package zank.mods.kube_packages.bridge.kubejs;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.bridge.KubePackagePaths;
import zank.mods.kube_packages.bridge.kubejs.binding.KubePackagesBinding;
import zank.mods.kube_packages.impl.dummy.DummyKubePackage;
import zank.mods.kube_packages.impl.dummy.DummyKubePackageProvider;
import zank.mods.kube_packages.impl.mod.ModKubePackageProvider;
import zank.mods.kube_packages.impl.path.DirKubePackageProvider;
import zank.mods.kube_packages.impl.zip.ZipKubePackageProvider;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.GameUtil;

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
        event.add("KubePackages", new KubePackagesBinding(event.getType()));
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
            new DummyKubePackageProvider(List.of(
                new DummyKubePackage(createKubeJSMetadata(), cx -> null)
            ))
        );
    }

    private static PackageMetadata createKubeJSMetadata() {
        var info = GameUtil.findModInfoOrThrow(KubeJS.MOD_ID);
        return PackageMetadata.builder()
            .id(KubeJS.MOD_ID)
            .name("KubeJS")
            .license(info.getOwningFile().getLicense())
            .version(info.getVersion())
            .description("""
                Dummy package injected by KubePackages itself, for easier dependency management""")
            .build();
    }

    @Override
    public void clearCaches() {
        KubePackages.clearPackages();
    }
}
