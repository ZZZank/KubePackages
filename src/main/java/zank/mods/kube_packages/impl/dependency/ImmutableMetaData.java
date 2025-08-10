package zank.mods.kube_packages.impl.dependency;

import lombok.NonNull;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.*;

/**
 * @author ZZZank
 */
public record ImmutableMetaData(
    @NonNull String id,
    @NonNull Optional<String> name,
    @NonNull Optional<String> description,
    @NonNull ArtifactVersion version,
    @NonNull Optional<String> license,
    @NonNull List<String> authors,
    @NonNull List<PackageDependency> dependencies
) implements PackageMetaData {
}
