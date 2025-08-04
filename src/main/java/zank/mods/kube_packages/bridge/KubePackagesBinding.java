package zank.mods.kube_packages.bridge;

import com.google.common.collect.Maps;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.inject.SortablePackageHolder;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.impl.dependency.SortableKubePackage;
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
    private final SortablePackageHolder pkgHolder;

    public KubePackagesBinding(ScriptType type, SortablePackageHolder pkgHolder) {
        this.type = type;
        // "why not get packsHolder.kpkg$sortablePacks() in advance", you might ask
        // Well, at this stage, packs map in script manager is not initialized yet, so we defer accessing
        this.pkgHolder = pkgHolder;
    }

    public ScriptType type() {
        return type;
    }

    @Info("""
        @return `true` if a ContentPack with provided `id` is present, `false` otherwise""")
    public boolean isLoaded(String id) {
        return pkgHolder.kpkg$sortablePacks().containsKey(id);
    }

    @Info("""
        @return The metadata from ContentPack with provided `id`, or `null` if there's no such ContentPack""")
    public PackageMetaData getMetadata(String id) {
        var sortableContentPack = pkgHolder.kpkg$sortablePacks().get(id);
        return Optional.ofNullable(sortableContentPack)
            .map(SortableKubePackage::pack)
            .map(KubePackage::getMetaData)
            .orElse(null);
    }

    @Info("""
        ContentPack id -> ContentPack metadata""")
    public Map<String, PackageMetaData> getAllMetadata() {
        return Collections.unmodifiableMap(Maps.transformValues(
            pkgHolder.kpkg$sortablePacks(),
            s -> s.pack().getMetaData()
        ));
    }

    @Info("""
        Put value into ContentPack shared data for **current** script type
        
        @see {@link type} Current script type
        @see {@link getAllSharedFor} View ContentPack shared data for another script type.""")
    public void putShared(String id, Object o) {
        TYPED_GLOBALS.get(type).put(id, o);
    }

    @Info("""
        Get ContentPack shared data for **current** script type
        
        @see {@link type} Current script type
        @see {@link getAlSharedFor} View ContentPack shared data for another script type.""")
    public Object getShared(String id) {
        return getShared(this.type, id);
    }

    @Info("""
        Get ContentPack shared data for specified script type
        
        @see {@link type} Current script type
        @see {@link getAlSharedFor} View ContentPack shared data for another script type.""")
    public Object getShared(ScriptType type, String id) {
        return getAllSharedFor(type).get(id);
    }

    @Info("""
        View all ContentPack shared data for **current** script type
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link type} Current script type
        @see {@link getAllSharedFor} View ContentPack shared data for another script type.""")
    public Map<String, Object> getAllSharedForCurrent() {
        return getAllSharedFor(type);
    }

    @Info("""
        View all ContentPack shared data for specified script type.
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link getAllSharedForCurrent} View all ContentPack shared data for **current** script type
        @see {@link putShared} Put value into ContentPack shared data for **current** script type""")
    public Map<String, Object> getAllSharedFor(ScriptType type) {
        return Collections.unmodifiableMap(TYPED_GLOBALS.get(type));
    }
}
