package zank.mods.kube_packages.bridge.kubejs;

import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.MetaDataBuilder;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZZZank
 */
public class MetadataBuilderJS extends MetaDataBuilder {

    public static final Map<String, PackageMetaData> BUILT_TEMP = new ConcurrentHashMap<>();

    public void buildAndWriteTo(String path) throws IOException {
        try (var writer = Files.newBufferedWriter(GameUtil.resolveSafe(path))) {
            var built = build();
            var json = PackageMetaData.CODEC.encodeStart(JsonOps.INSTANCE, built)
                .resultOrPartial(CodecUtil.THROW_ERROR)
                .orElseThrow();
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(json, jsonWriter);
        }
    }

    public void buildAndPushToCache() {
        var built = build();
        BUILT_TEMP.put(built.id(), built);
    }
}
