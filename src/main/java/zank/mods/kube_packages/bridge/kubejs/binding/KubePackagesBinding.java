package zank.mods.kube_packages.bridge.kubejs.binding;

import com.google.common.collect.Maps;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.api.meta.dependency.DependencyBuilder;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;
import zank.mods.kube_packages.bridge.kubejs.PackageExporter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public class KubePackagesBinding {
    public static final Map<ScriptType, Map<String, Object>> TYPED_GLOBALS = new EnumMap<>(ScriptType.class);

    static {
        for (var scriptType : ScriptType.values()) {
            TYPED_GLOBALS.put(scriptType, new ConcurrentHashMap<>());
        }
    }

    @Info("""
        Current script type""")
    public final ScriptType scriptType;
    @Info("""
        Used for storing data meant to be used by other packages, similar to `global`.
        
        Users can mutate shared data for current script type and view shared data for all script types""")
    public final KubePackagesSharedData sharedData;

    public KubePackagesBinding(ScriptType type) {
        this.scriptType = type;
        this.sharedData = new KubePackagesSharedData(type);
    }

    public MetadataBuilderJS metaDataBuilder() {
        return new MetadataBuilderJS();
    }

    public PackageMetadata metaDataMinimal(String id, ArtifactVersion version) {
        return PackageMetadata.minimal(id, version);
    }

    public DependencyBuilder dependencyBuilder() {
        return PackageDependency.builder();
    }

    public PackageExporter packageExporter() {
        var console = this.scriptType.console;
        return new PackageExporter(message -> console.info(message.getString()));
    }

    @Info("""
        @return `true` if a KubePackage with provided `id` is present, `false` otherwise""")
    public boolean isLoaded(String id) {
        return KubePackages.getPackages().containsKey(id);
    }

    @Info("""
        @return The metadata from KubePackage with provided `id`, or `null` if there's no such KubePackage""")
    public PackageMetadata getMetadata(String id) {
        var pkg = KubePackages.getPackages().get(id);
        return pkg == null ? null : pkg.metadata();
    }

    @Info("""
        KubePackage id -> KubePackage metadata""")
    public Map<String, PackageMetadata> viewAllMetadata() {
        return Maps.transformValues(KubePackages.getPackages(), KubePackage::metadata);
    }
}
