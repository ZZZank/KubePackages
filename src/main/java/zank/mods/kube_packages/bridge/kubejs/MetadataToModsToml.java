package zank.mods.kube_packages.bridge.kubejs;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.utils.JavaOps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class MetadataToModsToml {

    public static Config convert(PackageMetadata metaData, Consumer<SimulatedModsToml> modifier) {
        var built = SimulatedModsToml.buildFromPackage(metaData);
        if (modifier != null) {
            modifier.accept(built);
        }

        var json = SimulatedModsToml.GSON.toJsonTree(built);
        var o = JsonOps.INSTANCE.convertTo(JavaOps.INSTANCE, json);

        return (Config) transformForConfig(TomlFormat::newConfig, o);
    }

    private static Object transformForConfig(Supplier<Config> configGen, Object value) {
        if (value instanceof List<?> l) {
            var transformed = new ArrayList<>(l.size());
            for (var o : l) {
                o = transformForConfig(configGen, o);
                if (o != null) {
                    transformed.add(o);
                }
            }
            return transformed;
        } else if (value instanceof Map<?, ?> m) {
            var transformed = configGen.get();
            for (var entry : m.entrySet()) {
                var o = transformForConfig(configGen, entry.getValue());
                if (o != null) {
                    transformed.add((String) entry.getKey(), o);
                }
            }
            return transformed;
        }
        return value;
    }
}
