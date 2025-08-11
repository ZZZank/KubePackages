package zank.mods.kube_packages.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
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

    public static DataResult<PackageMetaData> readMetaData(Reader reader) {
        var json = KubePackages.GSON.fromJson(reader, JsonObject.class);
        return PackageMetaData.CODEC.parse(JsonOps.INSTANCE, json);
    }

    public static DataResult<PackageMetaData> readMetaData(InputStream stream) {
        try (var reader = FileUtil.stream2reader(stream)) {
            return readMetaData(reader);
        } catch (IOException e) {
            return DataResult.error(e::toString);
        }
    }

    public static PackageMetaData readMetaDataOrThrow(InputStream stream) {
        return readMetaData(stream)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }

    public static PackageMetaData readMetaDataOrThrow(Reader reader) {
        return readMetaData(reader)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }
}
