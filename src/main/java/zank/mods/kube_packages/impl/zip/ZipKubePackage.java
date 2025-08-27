package zank.mods.kube_packages.impl.zip;

import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.impl.KubePackageBase;
import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipFile;

public class ZipKubePackage extends KubePackageBase {
    private final Path path;

    public ZipKubePackage(Path path, PackageMetadata metaData) {
        super(metaData);
        this.path = path;
    }

    @Override
    @Nullable
    protected ScriptPack createPack(ScriptLoadContext context) {
        var pack = KubePackageUtils.createEmptyPack(context, id());
        var prefix = context.folderName() + '/';
        try (var zipFile = new ZipFile(path.toFile())) {
            zipFile.stream()
                .filter(e -> !e.isDirectory())
                .filter(e -> e.getName().endsWith(".js"))
                .filter(e -> e.getName().startsWith(prefix))
                .forEach(zipEntry -> {
                    var zipFileInfo = new ScriptFileInfo(pack.info, zipEntry.getName().substring(prefix.length()));
                    var scriptSource = (ScriptSource) info -> {
                        var reader = new BufferedReader(new InputStreamReader(
                            zipFile.getInputStream(zipEntry),
                            StandardCharsets.UTF_8
                        ));
                        return reader.lines().toList();
                    };
                    context.loadFileIntoPack(pack, zipFileInfo, scriptSource);
                });
            return pack;
        } catch (IOException e) {
            // TODO: log
            return null;
        }
    }

    @Override
    public @Nullable PackResources getResource(PackType type) {
        return new FilePackResources(id(), path.toFile(), false);
    }

    @Override
    public String toString() {
        return "ZipKubePackage[%s]".formatted(metaData.id());
    }
}
