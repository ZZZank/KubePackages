package zank.mods.kube_packages.impl.dependency;

import lombok.NonNull;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.LoadOrdering;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;

/**
 * @author ZZZank
 */
public record ImmutableDependency(
    @NonNull DependencyType type,
    @NonNull DependencySource source,
    @NonNull String id,
    @NonNull Optional<VersionRange> versionRange,
    @NonNull Optional<String> reason,
    @NonNull Optional<LoadOrdering> ordering
) implements PackageDependency {
}
