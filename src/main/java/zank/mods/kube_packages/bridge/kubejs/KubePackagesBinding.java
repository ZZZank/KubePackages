package zank.mods.kube_packages.bridge.kubejs;

import com.google.common.collect.Maps;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.DependencyBuilder;
import zank.mods.kube_packages.api.meta.MetaDataBuilder;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;

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

    private final ScriptType type;

    public KubePackagesBinding(ScriptType type) {
        this.type = type;
    }

    public MetaDataBuilder metaDataBuilder() {
        return PackageMetaData.builder();
    }

    public PackageMetaData metaDataMinimal(String id, ArtifactVersion version) {
        return PackageMetaData.minimal(id, version);
    }

    public DependencyBuilder dependencyBuilder() {
        return PackageDependency.builder();
    }

    public PackageExporter packageExporter() {
        return new PackageExporter(message -> this.type.console.info(message.getString()));
    }

    @Info("""
        @return `true` if a KubePackage with provided `id` is present, `false` otherwise""")
    public boolean isLoaded(String id) {
        return KubePackages.getPackages().containsKey(id);
    }

    @Info("""
        @return The metadata from KubePackage with provided `id`, or `null` if there's no such KubePackage""")
    public PackageMetaData getMetadata(String id) {
        var pkg = KubePackages.getPackages().get(id);
        return pkg == null ? null : pkg.metaData();
    }

    @Info("""
        KubePackage id -> KubePackage metadata""")
    public Map<String, PackageMetaData> viewAllMetadata() {
        return Maps.transformValues(KubePackages.getPackages(), KubePackage::metaData);
    }

    @Info("""
        Put value into KubePackage shared data for **current** script type
        
        @see {@link type} Current script type
        @see {@link getAllSharedFor} View KubePackage shared data for another script type.""")
    public void putShared(String id, Object o) {
        TYPED_GLOBALS.get(type).put(id, o);
    }

    @Info("""
        Get KubePackage shared data for **current** script type
        
        @see {@link type} Current script type
        @see {@link getAlSharedFor} View KubePackage shared data for another script type.""")
    public Object getShared(String id) {
        return getShared(this.type, id);
    }

    @Info("""
        Get KubePackage shared data for specified script type
        
        @see {@link type} Current script type
        @see {@link getAlSharedFor} View KubePackage shared data for another script type.""")
    public Object getShared(ScriptType type, String id) {
        return getAllSharedFor(type).get(id);
    }

    @Info("""
        View all KubePackage shared data for **current** script type
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link type} Current script type
        @see {@link getAllSharedFor} View KubePackage shared data for another script type.""")
    public Map<String, Object> getAllSharedForCurrent() {
        return getAllSharedFor(type);
    }

    @Info("""
        View all KubePackage shared data for specified script type.
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link getAllSharedForCurrent} View all KubePackage shared data for **current** script type
        @see {@link putShared} Put value into KubePackage shared data for **current** script type""")
    public Map<String, Object> getAllSharedFor(ScriptType type) {
        return Collections.unmodifiableMap(TYPED_GLOBALS.get(type));
    }
}
