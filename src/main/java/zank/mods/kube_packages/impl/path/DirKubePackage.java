package zank.mods.kube_packages.impl.path;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;

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
            var metaData = KubePackageUtils.readMetadataOrThrow(reader);
            return new DirKubePackage(base, metaData);
        } catch (Exception e) {
            return null;
        }
    }

    private final Path base;
    private final PackageMetadata metaData;

    public DirKubePackage(Path base, PackageMetadata metaData) {
        this.base = base;
        this.metaData = metaData;
    }

    @Override
    public PackageMetadata metadata() {
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
    public @Nullable PackResources getResource(PackType type) {
        return new PathPackResources(id(), this.base, false);
    }

    @Override
    public String toString() {
        return "DirKubePackage[%s]".formatted(id());
    }
}
