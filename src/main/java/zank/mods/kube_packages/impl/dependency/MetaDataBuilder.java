package zank.mods.kube_packages.impl.dependency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;

import java.util.List;
import java.util.Optional;

/**
 * @author ZZZank
 */
@RequiredArgsConstructor
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class MetaDataBuilder {
    private String id;
    private Optional<String> name = Optional.empty();
    private Optional<String> description = Optional.empty();
    private Optional<ArtifactVersion> version = Optional.empty();
    private List<String> authors = List.of();
    private List<PackageDependency> dependencies = List.of();

    public PackageMetaData build() {
        return new ImmutableMetaData(id, name, description, version, authors, dependencies);
    }
}
