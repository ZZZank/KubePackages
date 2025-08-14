package zank.mods.kube_packages.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptPackInfo;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author ZZZank
 */
public class KubePackageUtils {
    public static ScriptPack createEmptyPack(ScriptLoadContext context, String id) {
        return new ScriptPack(context.manager(), new ScriptPackInfo(id, ""));
    }

    public static DataResult<PackageMetadata> readMetadata(Reader reader) {
        var json = KubePackages.GSON.fromJson(reader, JsonObject.class);
        return PackageMetadata.CODEC.parse(JsonOps.INSTANCE, json);
    }

    public static DataResult<PackageMetadata> readMetadata(InputStream stream) {
        try (var reader = FileUtil.stream2reader(stream)) {
            return readMetadata(reader);
        } catch (IOException e) {
            return DataResult.error(e::toString);
        }
    }

    public static PackageMetadata readMetadataOrThrow(InputStream stream) {
        return readMetadata(stream)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }

    public static PackageMetadata readMetadataOrThrow(Reader reader) {
        return readMetadata(reader)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }
}
