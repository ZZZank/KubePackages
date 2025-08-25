package zank.mods.kube_packages.bridge.kubejs.binding;

import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public record KubePackagesSharedData(ScriptType scriptType) {
    public static final Map<ScriptType, Map<String, Object>> TYPED_GLOBALS = new EnumMap<>(ScriptType.class);

    static {
        for (var scriptType : ScriptType.values()) {
            TYPED_GLOBALS.put(scriptType, new ConcurrentHashMap<>());
        }
    }

    @Info("""
        Put value into KubePackage shared data for **current** script type
        
        @see {@link scriptType} Current script type
        @see {@link getAllSharedFor} View KubePackage shared data for another script type.""")
    public void put(String id, Object o) {
        TYPED_GLOBALS.get(scriptType).put(id, o);
    }

    @Info("""
        Get KubePackage shared data for **current** script type
        
        @see {@link scriptType} Current script type
        @see {@link getAlSharedFor} View KubePackage shared data for another script type.""")
    public Object get(String id) {
        return getForType(this.scriptType, id);
    }

    @Info("""
        Get KubePackage shared data for specified script type
        
        @see {@link scriptType} Current script type
        @see {@link getAlSharedFor} View KubePackage shared data for another script type.""")
    public Object getForType(ScriptType type, String id) {
        return getAllForType(type).get(id);
    }

    @Info("""
        View all KubePackage shared data for **current** script type
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link scriptType} Current script type
        @see {@link getAllSharedFor} View KubePackage shared data for another script type.""")
    public Map<String, Object> getAll() {
        return getAllForType(scriptType);
    }

    @Info("""
        View all KubePackage shared data for specified script type.
        
        The return value is **immutable**, which means you can't put value into it
        
        @see {@link getAllSharedForCurrent} View all KubePackage shared data for **current** script type
        @see {@link putShared} Put value into KubePackage shared data for **current** script type""")
    public Map<String, Object> getAllForType(ScriptType type) {
        return Collections.unmodifiableMap(TYPED_GLOBALS.get(type));
    }
}
