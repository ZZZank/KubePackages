package zank.mods.kube_packages.impl.path;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class DirKubePackage implements KubePackage {
    public static DirKubePackage tryLoad(Path base) {
        var path = base.resolve(KubePackages.META_DATA_FILE_NAME);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return null;
        }
        try (var reader = Files.newBufferedReader(path)) {
            var metaData = KubePackageUtils.readMetaDataOrThrow(reader);
            return new DirKubePackage(base, metaData);
        } catch (Exception e) {
            return null;
        }
    }

    private final Path base;
    private final PackageMetaData metaData;

    public DirKubePackage(Path base, PackageMetaData metaData) {
        this.base = base;
        this.metaData = metaData;
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
