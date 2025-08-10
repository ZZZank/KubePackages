package zank.mods.kube_packages.impl.dependency;

import com.mojang.serialization.Codec;
import lombok.NonNull;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.*;

/**
 * @author ZZZank
 */
public record ImmutableMetaData(
    @NonNull String id,
    @NonNull Optional<String> name,
    @NonNull Optional<String> description,
    @NonNull Optional<ArtifactVersion> version,
    @NonNull Optional<String> license,
    @NonNull List<String> authors,
    @NonNull List<PackageDependency> dependencies
) implements PackageMetaData {
    public static final Codec<ArtifactVersion> VERSION_CODEC = Codec.STRING.xmap(DefaultArtifactVersion::new, ArtifactVersion::toString);
}
