package zank.mods.kube_packages.impl.dependency;

import com.mojang.serialization.Codec;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.LoadOrdering;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.utils.CodecUtil;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;

/**
 * @author ZZZank
 */
public record ImmutableDependency(
    DependencyType type,
    DependencySource source,
    String id,
    Optional<VersionRange> versionRange,
    Optional<String> reason,
    Optional<LoadOrdering> ordering
) implements PackageDependency {
    public static final Codec<VersionRange> VERSION_RANGE_CODEC = Codec.STRING.comapFlatMap(
        CodecUtil.wrapUnsafeFn(VersionRange::createFromVersionSpec),
        VersionRange::toString
    );
}
