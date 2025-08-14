package zank.mods.kube_packages.api.meta;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;

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
    private String name = null;
    private String description = null;
    private ArtifactVersion version = null;
    private String license = null;
    private List<String> authors = List.of();
    private List<PackageDependency> dependencies = List.of();

    public PackageMetaData build() {
        return new ImmutableMetaData(
            id,
            Optional.ofNullable(name),
            Optional.ofNullable(description),
            version,
            Optional.ofNullable(license),
            authors,
            dependencies
        );
    }
}
