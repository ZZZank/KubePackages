package zank.mods.kube_packages.api.meta;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import zank.mods.kube_packages.utils.CodecUtil;

import java.util.List;
import java.util.Optional;

/**
 * @author ZZZank
 */
public interface PackageMetaData {

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

    static MetaDataBuilder builder() {
        return new MetaDataBuilder();
    }

    static PackageMetaData minimal(String id, ArtifactVersion version) {
        return new MetaDataBuilder().id(id).version(version).build();
    }

    Codec<PackageMetaData> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            Codec.STRING.fieldOf("id").forGetter(PackageMetaData::id),
            Codec.STRING.optionalFieldOf("name").forGetter(PackageMetaData::name),
            Codec.STRING.optionalFieldOf("description").forGetter(PackageMetaData::description),
            CodecUtil.VERSION_CODEC.fieldOf("version").forGetter(PackageMetaData::version),
            Codec.STRING.optionalFieldOf("license").forGetter(PackageMetaData::license),
            Codec.STRING.listOf().optionalFieldOf("authors", List.of()).forGetter(PackageMetaData::authors),
            PackageDependency.CODEC.listOf()
                .optionalFieldOf("dependencies", List.of())
                .forGetter(PackageMetaData::dependencies)
        ).apply(builder, ImmutableMetaData::new)
    );
}
