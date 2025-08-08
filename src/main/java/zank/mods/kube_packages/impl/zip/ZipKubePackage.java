package zank.mods.kube_packages.impl.zip;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.impl.KubePackageBase;
import dev.latvian.mods.kubejs.script.ScriptFileInfo;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptSource;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

public class ZipKubePackage extends KubePackageBase {
    private final Path path;

    public ZipKubePackage(Path path, PackageMetaData metaData) {
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
                    var zipFileInfo = new ScriptFileInfo(pack.info, zipEntry.getName());
                    var scriptSource = (ScriptSource) info -> {
                        var reader = new BufferedReader(new InputStreamReader(
                            zipFile.getInputStream(zipEntry), StandardCharsets.UTF_8));
                        return reader.lines().toList();
                    };
                    context.loadFile(pack, zipFileInfo, scriptSource);
                });
            return pack;
        } catch (IOException e) {
            // TODO: log
            return null;
        }
    }

    @Override
    public void getResource(PackType type, Consumer<Pack> packLoader) {
        var pack = Pack.create(
            this.id(),
            Component.literal(toString()),
            true,
            name -> new FilePackResources(name, this.path.toFile(), false),
            new Pack.Info(
                Component.literal("Resource collected by " + toString()),
                SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA),
                SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES),
                FeatureFlagSet.of(FeatureFlags.BUNDLE),
                false
            ),
            type,
            Pack.Position.BOTTOM,
            true,
            PackSource.DEFAULT
        );
        packLoader.accept(pack);
    }

    @Override
    public String toString() {
        return "ZipKubePackage[namespace=%s]".formatted(metaData.id());
    }
}
