package test;

import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.google.common.base.Suppliers;
import com.mojang.serialization.JsonOps;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.bridge.kubejs.export.MetadataToModsToml;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;
import zank.mods.kube_packages.utils.GameUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class DataExportTest {

    private static final Supplier<PackageMetaData> META_DATA = Suppliers.memoize(
        () -> new ImmutableMetaData(
            "examplemod",
            Optional.of("Exampl"),
            Optional.of("""
                wow description"""),
            new DefaultArtifactVersion("2.3.4"),
            Optional.empty(),
            List.of(),
            List.of(
                new ImmutableDependency(
                    DependencyType.REQUIRED,
                    DependencySource.MOD,
                    "forge",
                    Optional.of(GameUtil.versionRangeFromSpecOrThrow("[47,)")),
                    Optional.of("forge the modloader, you obviously need it"),
                    Optional.empty()
                )
            )
        )
    );

    public void modsToml() {
        var path = Path.of("run", "mods.toml");
        TomlFormat.instance().createWriter().write(
            MetadataToModsToml.convert(META_DATA.get(), null),
            path,
            WritingMode.REPLACE
        );
        System.out.println("mods.toml exported to: " + path.toAbsolutePath());
    }

    public void metadata() {
        var path = Path.of("run", KubePackages.META_DATA_FILE_NAME);
        try (var writer = Files.newBufferedWriter(path)) {
            var json = PackageMetaData.CODEC
                .encodeStart(JsonOps.INSTANCE, META_DATA.get())
                .result()
                .orElseThrow();
            var jsonWriter = KubePackages.GSON.newJsonWriter(writer);
            jsonWriter.setIndent("    ");
            KubePackages.GSON.toJson(json, jsonWriter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new DataExportTest().metadata();
    }
}
