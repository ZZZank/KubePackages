package zank.mods.kube_packages.api.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetadata;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.utils.CodecUtil;

import java.util.List;
import java.util.Optional;

/**
 * @author ZZZank
 */
public interface PackageMetadata {

    @NotNull
    String id();

    @NotNull
    Optional<String> name();

    default String displayName() {
        return name().orElse(id());
    }

    @NotNull
    Optional<String> description();

    @NotNull
    ArtifactVersion version();

    @NotNull
    Optional<String> license();

    @NotNull
    List<String> authors();

    @NotNull
    List<PackageDependency> dependencies();

    static MetadataBuilder builder() {
        return new MetadataBuilder();
    }

    static PackageMetadata minimal(String id, ArtifactVersion version) {
        return new MetadataBuilder().id(id).version(version).build();
    }

    Codec<PackageMetadata> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Codec.STRING.fieldOf("id").forGetter(PackageMetadata::id),
            Codec.STRING.optionalFieldOf("name").forGetter(PackageMetadata::name),
            Codec.STRING.optionalFieldOf("description").forGetter(PackageMetadata::description),
            CodecUtil.VERSION_CODEC.fieldOf("version").forGetter(PackageMetadata::version),
            Codec.STRING.optionalFieldOf("license").forGetter(PackageMetadata::license),
            Codec.STRING.listOf().optionalFieldOf("authors", List.of()).forGetter(PackageMetadata::authors),
            PackageDependency.CODEC.listOf()
                .optionalFieldOf("dependencies", List.of())
                .forGetter(PackageMetadata::dependencies)
        ).apply(builder, ImmutableMetadata::new)
    );
}
