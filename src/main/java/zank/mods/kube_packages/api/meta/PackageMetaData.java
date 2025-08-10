package zank.mods.kube_packages.api.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.List;
import java.util.Optional;

/**
 * @author ZZZank
 */
public interface PackageMetaData {

    String id();

    Optional<String> name();

    Optional<String> description();

    Optional<ArtifactVersion> version();

    Optional<String> license();

    List<String> authors();

    List<PackageDependency> dependencies();

    static MetaDataBuilder builder() {
        return new MetaDataBuilder();
    }

    static PackageMetaData minimal(String id) {
        return new MetaDataBuilder().id(id).build();
    }

    Codec<PackageMetaData> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Codec.STRING.fieldOf("id").forGetter(PackageMetaData::id),
            Codec.STRING.optionalFieldOf("name").forGetter(PackageMetaData::name),
            Codec.STRING.optionalFieldOf("description").forGetter(PackageMetaData::description),
            ImmutableMetaData.VERSION_CODEC.optionalFieldOf("version").forGetter(PackageMetaData::version),
            Codec.STRING.optionalFieldOf("license").forGetter(PackageMetaData::license),
            Codec.STRING.listOf().optionalFieldOf("authors", List.of()).forGetter(PackageMetaData::authors),
            PackageDependency.CODEC.listOf()
                .optionalFieldOf("dependencies", List.of())
                .forGetter(PackageMetaData::dependencies)
        ).apply(builder, ImmutableMetaData::new)
    );
}
