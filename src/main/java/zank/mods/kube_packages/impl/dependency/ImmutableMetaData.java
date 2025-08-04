package zank.mods.kube_packages.impl.dependency;

import com.mojang.serialization.Codec;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.*;

/**
 * @author ZZZank
 */
public record ImmutableMetaData(
    String id,
    Optional<String> name,
    Optional<String> description,
    Optional<ArtifactVersion> version,
    List<String> authors,
    List<PackageDependency> dependencies
) implements PackageMetaData {
    public static final Codec<ArtifactVersion> VERSION_CODEC = Codec.STRING.xmap(DefaultArtifactVersion::new, ArtifactVersion::toString);
}
