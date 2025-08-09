package zank.mods.kube_packages.api.meta.dependency;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;

import java.util.Optional;

/**
 * @author ZZZank
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class DependencyBuilder {
    private DependencyType type;
    private DependencySource source = DependencySource.PACK;
    private String id;
    private VersionRange versionRange = null;
    private String reason = null;
    private LoadOrdering ordering = null;

    public PackageDependency build() {
        return new ImmutableDependency(
            type,
            source,
            id,
            Optional.ofNullable(versionRange),
            Optional.ofNullable(reason),
            Optional.ofNullable(ordering)
        );
    }
}
