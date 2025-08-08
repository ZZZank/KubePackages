package zank.mods.kube_packages.impl.dependency;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.LoadOrdering;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;

import java.util.Optional;

/**
 * @author ZZZank
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(fluent = true)
public class DependencyBuilder {
    private DependencyType type;
    private DependencySource source = DependencySource.PACK;
    private String id;
    private Optional<VersionRange> versionRange = Optional.empty();
    private Optional<String> reason = Optional.empty();
    private Optional<LoadOrdering> ordering = Optional.empty();

    public PackageDependency build() {
        return new ImmutableDependency(type, source, id, versionRange, reason, ordering);
    }
}
