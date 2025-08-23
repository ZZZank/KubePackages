package zank.mods.kube_packages.bridge.kubejs;

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
import org.apache.commons.io.file.*;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.bridge.kubejs.binding.PathFilterHelper;
import zank.mods.kube_packages.bridge.kubejs.toml.MetadataToModsToml;
import zank.mods.kube_packages.bridge.kubejs.toml.SimulatedModsToml;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.DirCopyVisitor;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private PackageMetadata metadata;
    private Consumer<SimulatedModsToml> modInfoModifier;
    private boolean debugMode;

    @Getter(AccessLevel.NONE)
    private final Map<Enum<?>, PathFilter> fileFilters = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<Enum<?>, PathFilter> dirFilters = new HashMap<>();

    public PackageExporter(Consumer<Component> reporter) {
        this.reporter = reporter;
    }

    public void runAsync() {
        var t = new Thread(
            () -> {
                try {
                    this.run();
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
        debug(Component.literal(message));
    }

    private void debug(Component message) {
        if (this.debugMode) {
            report(message);
        }
    }

    private static Path ensureDir(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public void setScriptFileFilter(ScriptType type, Function<PathFilterHelper, IOFileFilter> toFilter) {
        this.fileFilters.put(type, toFilter.apply(PathFilterHelper.INSTANCE));
    }

    public void setScriptDirFilter(ScriptType type, Function<PathFilterHelper, IOFileFilter> toFilter) {
        this.dirFilters.put(type, toFilter.apply(PathFilterHelper.INSTANCE));
    }

    public void setAssetFileFilter(PackType type, Function<PathFilterHelper, IOFileFilter> toFilter) {
        this.fileFilters.put(type, toFilter.apply(PathFilterHelper.INSTANCE));
    }

    public void setAssetDirFilter(PackType type, Function<PathFilterHelper, IOFileFilter> toFilter) {
        this.dirFilters.put(type, toFilter.apply(PathFilterHelper.INSTANCE));
    }

    private void copyScriptAndAsset(Path root) throws IOException {
        for (var scriptType : scriptTypes) {
            var directoryName = GameUtil.toFolderName(scriptType);
            var source = KubeJSPaths.DIRECTORY.resolve(directoryName).toAbsolutePath();
            var target = ensureDir(root.resolve(directoryName));

            var counters = PathUtils.visitFileTree(
                new DirCopyVisitor(
                    Counters.longPathCounters(),
                    getFilter(fileFilters, scriptType),
                    getFilter(dirFilters, scriptType),
                    source,
                    target
                ),
                source
            ).counters();
            debug(Component.translatable("Copied %s files for %s", counters.getFileCounter().get(), scriptType));
        }

        for (var resourceType : resourceTypes) {
            var directory = resourceType.getDirectory();
            var source = KubeJSPaths.DIRECTORY.resolve(directory).toAbsolutePath();
            var target = ensureDir(root.resolve(directory));

            var counters = PathUtils.visitFileTree(
                new DirCopyVisitor(
                    Counters.longPathCounters(),
                    getFilter(fileFilters, resourceType),
                    getFilter(dirFilters, resourceType),
                    source,
                    target
                ),
                source
            ).counters();
            debug(Component.translatable("Copied %s files for %s", counters.getFileCounter().get(), resourceType));
        }
    }

    private PathFilter getFilter(Map<Enum<?>, PathFilter> source, Enum<?> key) {
        var got = source.get(key);
        return got == null ? FileFilterUtils.trueFileFilter() : got;
    }

    private void writeMetadata(Path root) throws IOException {
        try (var writer = Files.newBufferedWriter(root.resolve(KubePackages.META_DATA_FILE_NAME))) {
            var encoded = PackageMetadata.CODEC
                .encodeStart(JsonOps.INSTANCE, this.metadata)
                .getOrThrow(false, CodecUtil.THROW_ERROR);
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(encoded, jsonWriter);
        }
        debug("Package metadata exported");
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
        var rawOut = new BufferedOutputStream(Files.newOutputStream(path));
        if (this.exportAs == ExportType.ZIP) {
            return new ZipOutputStream(rawOut);
        }

        var manifest = new Manifest();
        var attributes = manifest.getMainAttributes();
        Map.of(
            "Manifest-Version", "1.0",
            "Specification-Title", metadata.id(),
            "Specification-Vendor", String.join(", ", metadata.authors()),
            "Specification-Version", "1",
            "Implementation-Title", metadata.displayName(),
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
