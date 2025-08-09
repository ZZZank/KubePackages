package zank.mods.kube_packages.impl.path;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;
import org.jetbrains.annotations.Nullable;
import zank.mods.kube_packages.utils.AssetUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class DirKubePackage implements KubePackage {
    public static boolean validate(Path base) {
        var path = base.resolve(KubePackages.META_DATA_FILE_NAME);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    private final Path base;
    private final PackageMetaData metaData;

    public DirKubePackage(Path base) {
        this.base = base;
        try (var reader = Files.newBufferedReader(base.resolve(KubePackages.META_DATA_FILE_NAME))) {
            this.metaData = PackageMetaData.CODEC.parse(
                    JsonOps.INSTANCE,
                    KubePackages.GSON.fromJson(reader, JsonObject.class)
                )
                .resultOrPartial(error -> {
                    throw new RuntimeException("Error when parsing metadata: " + error);
                })
                .orElseThrow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PackageMetaData getMetaData() {
        return metaData;
    }

    @Override
    public @Nullable ScriptPack getScript(ScriptLoadContext context) {
        var scriptPath = base.resolve(context.folderName());
        if (!Files.isDirectory(scriptPath)) {
            return null;
        }
        var pack = KubePackageUtils.createEmptyPack(context, id());
        KubeJS.loadScripts(pack, scriptPath, "");
        for (var fileInfo : pack.info.scripts) {
            var scriptSource = (ScriptSource.FromPath) (info) -> scriptPath.resolve(info.file);
            context.loadFileIntoPack(pack, fileInfo, scriptSource);
        }
        return pack;
    }

    @Override
    public void getResource(PackType type, Consumer<Pack> packLoader) {
        var path = this.base.resolve(type.getDirectory());
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return;
        }
        var pack = AssetUtil.packForPackage(
            this,
            Component.literal(toString()),
            Component.literal("Resource collected by " + toString()),
            type,
            name -> new PathPackResources(name, this.base, false)
        );
        packLoader.accept(pack);
    }

    @Override
    public String toString() {
        return "DirKubePackage[%s]".formatted(id());
    }
}
