package zank.mods.kube_packages.utils;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class FileUtil {

    public static PackageMetaData loadMetaData(Path base) {
        try (var reader = Files.newBufferedReader(base.resolve(KubePackages.META_DATA_FILE_NAME))) {
            var result = PackageMetaData.CODEC.parse(
                JsonOps.INSTANCE,
                KubePackages.GSON.fromJson(reader, JsonObject.class)
            );
            if (result.result().isPresent()) {
                return result.result().get();
            }
            var errorMessage = result.error().orElseThrow().message();
            throw new RuntimeException("Error when parsing metadata: " + errorMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader stream2reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
