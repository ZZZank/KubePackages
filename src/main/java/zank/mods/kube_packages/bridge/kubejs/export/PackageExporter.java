package zank.mods.kube_packages.bridge.kubejs.export;

import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.serialization.JsonOps;
import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import org.apache.commons.io.file.PathUtils;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author ZZZank
 */
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class PackageExporter {
    @Getter(AccessLevel.NONE)
    private final Consumer<Component> reporter;

    private String exportName;
    private ScriptType[] scriptTypes;
    private PackType[] resourceTypes;
    private ExportType exportAs;
    private PackageMetaData metadata;
    private Consumer<SimulatedModsToml> modInfoModifier;
    private boolean debugMode;

    public PackageExporter(Consumer<Component> reporter) {
        this.reporter = reporter;
    }

    public void runAsync() {
        var t = new Thread(
            () -> {
                try {
                    this.run();
                    report(Component.literal("Package exported to: " + Platform.getGameFolder()
                        .relativize(KubeJSPaths.EXPORT.resolve(this.exportName))));
                } catch (IOException e) {
                    report(Component.literal("Error when exporting packages: ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(e.toString()))
                    );
                }
            }, "Exporter-" + this.exportName
        );
        t.setDaemon(true);
        t.start();
    }

    public void run() throws IOException {
        Objects.requireNonNull(this.exportAs, "exportAs == null");
        Objects.requireNonNull(this.metadata, "metadata == null");

        if (this.exportName == null) {
            this.exportName = "KubePackages-" + metadata.id() + '-' + metadata.version();
        }
        if (this.scriptTypes == null) {
            this.scriptTypes = ScriptType.values();
        }
        if (this.resourceTypes == null) {
            this.resourceTypes = PackType.values();
        }

        var copyTo = KubeJSPaths.EXPORT.resolve(this.exportName);
        ensureDir(copyTo);
        PathUtils.cleanDirectory(copyTo);

        copyScriptAndAsset(copyTo);
        writeMetadata(copyTo);

        if (exportAs == ExportType.ZIP) {
            sealAsCompressedFile(copyTo, ".zip");
        } else if (exportAs == ExportType.MOD) {
            writeModInfos(copyTo);
            sealAsCompressedFile(copyTo, ".jar");
        }
    }

    private void report(Component message) {
        var reporter = this.reporter;
        if (reporter != null) {
            reporter.accept(message);
        }
    }

    private void debug(String message) {
        if (this.debugMode) {
            report(Component.literal(message));
        }
    }

    private static Path ensureDir(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    private void copyScriptAndAsset(Path root) throws IOException {
        for (var scriptType : scriptTypes) {
            var directory = GameUtil.toFolderName(scriptType);
            PathUtils.copyDirectory(
                KubeJSPaths.DIRECTORY.resolve(directory),
                ensureDir(root.resolve(directory))
            );
        }
        debug("scripts copied");

        for (var resourceType : resourceTypes) {
            var directory = resourceType.getDirectory();
            PathUtils.copyDirectory(
                KubeJSPaths.DIRECTORY.resolve(directory),
                ensureDir(root.resolve(directory))
            );
        }
        debug("assets copied");
    }

    private void writeMetadata(Path root) throws IOException {
        try (var writer = Files.newBufferedWriter(root.resolve(KubePackages.META_DATA_FILE_NAME))) {
            var encoded = PackageMetaData.CODEC
                .encodeStart(JsonOps.INSTANCE, this.metadata)
                .getOrThrow(false, CodecUtil.THROW_ERROR);
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(encoded, jsonWriter);
        }
        debug("package metadata exported");
    }

    private void writeModInfos(Path root) throws IOException {
        // mods.toml
        ensureDir(root.resolve("META-INF"));
        try (var writer = Files.newBufferedWriter(root.resolve("META-INF/mods.toml"))) {
            var got = MetadataToModsToml.convert(metadata, this.modInfoModifier);
            TomlFormat.instance()
                .createWriter()
                .write(got, writer);
        }
        debug("mods.toml generated");

        // pack.mcmeta
        try (var writer = Files.newBufferedWriter(root.resolve("pack.mcmeta"))) {
            var mcmeta = Map.of(
                "pack", Map.of(
                    "description", "Resources for %s(%s), from KubePackages",
                    "pack_format", 15
                )
            );
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(mcmeta, Map.class, jsonWriter);
        }
        debug("pack.mcmeta generated");
    }

    private ZipOutputStream getOutputStream(Path path) throws IOException {
        var rawOut = Files.newOutputStream(path);
        if (this.exportAs == ExportType.ZIP) {
            return new ZipOutputStream(rawOut);
        }

        var manifest = new Manifest();
        var attributes = manifest.getMainAttributes();
        Map.<String, String>of(
            "Specification-Title", metadata.id(),
            "Specification-Vendor", String.join(", ", metadata.authors()),
            "Specification-Version", "1",
            "Implementation-Title", metadata.name().orElse(metadata.id()),
            "Implementation-Version", metadata.version().toString(),
            "Implementation-Vendor", String.join(", ", metadata.authors()),
            "Implementation-Timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())
        ).forEach(attributes::putValue);
        return new JarOutputStream(rawOut, manifest);
    }

    private void sealAsCompressedFile(Path root, String suffix) throws IOException {
        var filePath = root.getParent().resolve(this.exportName + suffix);
        try (var zipOutput = getOutputStream(filePath);
             var fileInput = Files.walk(root).filter(Files::isRegularFile)
        ) {
            for (var path : (Iterable<Path>) fileInput::iterator) {
                var zipEntry = new ZipEntry(root.relativize(path).toString().replace(File.separatorChar, '/'));

                zipOutput.putNextEntry(zipEntry);
                try (var fIn = new FileInputStream(path.toFile())) {
                    fIn.transferTo(zipOutput);
                }
                zipOutput.closeEntry();
            }
        }
        debug("compressed file exported to: " + Platform.getGameFolder().relativize(filePath));
        PathUtils.deleteDirectory(root);
        debug("temp directory removed");
    }

    public enum ExportType {
        ZIP,
        MOD,
        DIR
    }
}
