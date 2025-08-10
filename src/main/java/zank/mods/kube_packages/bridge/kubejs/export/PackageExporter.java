package zank.mods.kube_packages.bridge.kubejs.export;

import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.server.packs.PackType;
import org.apache.commons.io.file.PathUtils;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
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
    private final ScriptType runningOn;

    private ScriptType[] scriptTypes;
    private PackType[] resourceTypes;
    private ExportType exportAs;
    private PackageMetaData metadata;
    private Consumer<SimulatedModsToml> modInfoModifier;

    public PackageExporter(ScriptType runningOn) {
        this.runningOn = Objects.requireNonNull(runningOn);
    }

    public void runAsync() {
        runningOn.executor.execute(() -> {
            try {
                this.run();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void run() throws IOException {
        Objects.requireNonNull(this.scriptTypes, "scriptTypes == null");
        Objects.requireNonNull(this.resourceTypes, "resourceTypes == null");
        Objects.requireNonNull(this.exportAs, "exportAs == null");
        Objects.requireNonNull(this.metadata, "metadata == null");

        var copyTo = KubeJSPaths.EXPORT.resolve("kube_packages-" + metadata.id());
        PathUtils.createParentDirectories(copyTo);

        copyScriptAndAsset(copyTo);
        writeMetadata(copyTo);
        if (exportAs == ExportType.ZIP) {
            sealAsCompressedFile(copyTo, ".zip");
        } else if (exportAs == ExportType.MOD) {
            writeModInfos(copyTo);
            sealAsCompressedFile(copyTo, ".jar");
        }
    }

    private void copyScriptAndAsset(Path root) throws IOException {
        var scriptTypes = this.scriptTypes == null ? ScriptType.values() : this.scriptTypes;
        for (var scriptType : scriptTypes) {
            var directory = GameUtil.toFolderName(scriptType);
            PathUtils.copyDirectory(
                KubeJSPaths.DIRECTORY.resolve(directory),
                root.resolve(directory)
            );
        }

        var resourceTypes = this.resourceTypes == null ? PackType.values() : this.resourceTypes;
        for (var resourceType : resourceTypes) {
            var directory = resourceType.getDirectory();
            PathUtils.copyDirectory(
                KubeJSPaths.DIRECTORY.resolve(directory),
                root.resolve(directory)
            );
        }
    }

    private void writeMetadata(Path root) throws IOException {
        try (var writer = Files.newBufferedWriter(root.resolve(KubePackages.META_DATA_FILE_NAME))) {
            var encoded = PackageMetaData.CODEC
                .encodeStart(JsonOps.INSTANCE, this.metadata)
                .getOrThrow(
                    false, error -> {
                        throw new RuntimeException(error);
                    }
                );
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(encoded, jsonWriter);
        }
    }

    private void writeModInfos(Path root) throws IOException {
        // mods.toml
        try (var writer = Files.newBufferedWriter(root.resolve("META-INF/mods.toml"))) {
            var got = MetadataToModsToml.convert(metadata, this.modInfoModifier);
            TomlFormat.instance()
                .createWriter()
                .write(got, writer);
        }

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
    }

    private ZipOutputStream getOutputStream(Path path) throws IOException {
        var rawOut = Files.newOutputStream(path);
        if (this.exportAs == ExportType.ZIP) {
            return new ZipOutputStream(rawOut);
        }

        var manifest = new Manifest();
        var attributes = manifest.getMainAttributes();
        attributes.putAll(Map.<String, String>of(
            "Specification-Title", metadata.id(),
            "Specification-Vendor", String.join(", ", metadata.authors()),
            "Specification-Version", "1",
            "Implementation-Title", metadata.name().orElse(metadata.id()),
            "Implementation-Version", metadata.version().toString(),
            "Implementation-Vendor", String.join(", ", metadata.authors()),
            "Implementation-Timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())
        ));
        return new JarOutputStream(rawOut, manifest);
    }

    private void sealAsCompressedFile(Path root, String suffix) throws IOException {

        try (var out = getOutputStream(root.getParent().resolve(root.getFileName() + suffix))) {
            var paths = (Iterable<Path>) Files.walk(root)::iterator;
            for (var path : paths) {
                var normalizedPath = root.relativize(path).toString().replace(File.separatorChar, '/');
                if (Files.isDirectory(path)) {
                    normalizedPath += '/';
                }

                var zipEntry = new ZipEntry(normalizedPath);

                out.putNextEntry(zipEntry);
                try (var fIn = new FileInputStream(path.toFile())) {
                    fIn.transferTo(out);
                }
                out.closeEntry();
            }
        }
    }

    public enum ExportType {
        ZIP,
        MOD,
        DIR
    }
}
