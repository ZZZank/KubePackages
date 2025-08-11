package zank.mods.kube_packages.bridge.kubejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.TreeTypeAdapter;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.jarjar.metadata.json.ArtifactVersionSerializer;
import net.minecraftforge.jarjar.metadata.json.VersionRangeSerializer;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.LoadOrdering;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.utils.GameUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
@SuppressWarnings("unused")
public class SimulatedModsToml {
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(VersionRange.class, new VersionRangeSerializer())
        .registerTypeAdapterFactory(TreeTypeAdapter.newTypeHierarchyFactory(
            ArtifactVersion.class,
            new ArtifactVersionSerializer()
        ))
        .create();

    private static final PackageDependency FORGE_DEP = PackageDependency.builder()
        .type(DependencyType.REQUIRED)
        .source(DependencySource.MOD)
        .id("forge")
        .versionRange(GameUtil.versionRangeFromSpecOrThrow("[47,)"))
        .build();

    private static final PackageDependency MINECRAFT_DEP = PackageDependency.builder()
        .type(DependencyType.REQUIRED)
        .source(DependencySource.MOD)
        .id("minecraft")
        .versionRange(GameUtil.versionRangeFromSpecOrThrow("[1.20.1,)"))
        .build();

    public static SimulatedModsToml buildFromPackage(PackageMetaData metadata) {
        var built = new SimulatedModsToml();
        built.license = metadata.license().orElse("Unknown License");
        built.mods = List.of(SimulatedModInfo.buildFromPackage(metadata));
        var packageDependencies = Stream.concat(
                metadata.dependencies().stream(),
                Stream.of(MINECRAFT_DEP, FORGE_DEP)
            )
            .collect(Collectors.toMap(
                PackageDependency::id,
                Function.identity(),
                (a, b) -> a
            ))
            .values()
            .stream()
            .map(SimulatedModDependency::buildFromPackage)
            .filter(Objects::nonNull)
            .toList();
        built.dependencies = Map.of(metadata.id(), packageDependencies);
        return built;
    }

    public String modLoader = "lowcodefml";
    public VersionRange loaderVersion = GameUtil.versionRangeFromSpecOrThrow("[47,)");
    public String license;
    public String issueTrackerURL;
    public List<SimulatedModInfo> mods;
    public Map<String, List<SimulatedModDependency>> dependencies;

    public static class SimulatedModInfo {
        public static SimulatedModInfo buildFromPackage(PackageMetaData metadata) {
            var built = new SimulatedModInfo();
            built.modId = metadata.id();
            built.version = metadata.version();
            built.displayName = metadata.name().orElse(metadata.id());
            built.authors = String.join(", ", metadata.authors());
            built.description = metadata.description().orElse(null);
            return built;
        }

        public String modId;
        public ArtifactVersion version;
        public String displayName;
        public String updateJSONURL = null;
        public String displayURL = null;
        public String logoFile = null;
        public String credits = null;
        public String authors = null;
        public String description;
    }

    public static class SimulatedModDependency {
        public static SimulatedModDependency buildFromPackage(PackageDependency dependency) {
            if (dependency.source() != DependencySource.MOD
                || (dependency.type() != DependencyType.REQUIRED && dependency.type() != DependencyType.OPTIONAL)) {
                return null;
            }
            var built = new SimulatedModDependency();
            built.modId = dependency.id();
            built.mandatory = dependency.type() == DependencyType.REQUIRED;
            built.versionRange = dependency.versionRange().orElse(IModInfo.UNBOUNDED);
            built.ordering = dependency.ordering().orElse(LoadOrdering.NONE);
            built.side = SimulatedDependencySide.BOTH;
            return built;
        }

        public String modId;
        public boolean mandatory;
        public VersionRange versionRange;
        public LoadOrdering ordering;
        public SimulatedDependencySide side;
    }

    public enum SimulatedDependencySide {
        CLIENT,
        SERVER,
        BOTH
    }
}
