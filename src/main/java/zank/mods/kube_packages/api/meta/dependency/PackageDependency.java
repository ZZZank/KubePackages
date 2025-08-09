package zank.mods.kube_packages.api.meta.dependency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.Optional;

/**
 * @author ZZZank
 */
public interface PackageDependency {

    static DependencyBuilder builder() {
        return new DependencyBuilder();
    }

    DependencyType type();

    DependencySource source();

    String id();

    Optional<VersionRange> versionRange();

    Optional<String> reason();

    Optional<LoadOrdering> ordering();

    default MutableComponent toReport(KubePackage parent) {
        return Component.translatable(
            "%s declared %s %s dependency with id '%s' and version range '%s'%s",
            Component.literal(parent.toString()).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE),
            Component.literal(this.type().toString()),
            Component.literal(this.source().toString()),
            Component.literal(this.id()).withStyle(ChatFormatting.YELLOW),
            Component.literal(this.versionRange().map(VersionRange::toString).orElse("*"))
                .withStyle(ChatFormatting.YELLOW),
            Component.literal(reason().map(", for reason '%s'"::formatted).orElse(""))
        );
    }

    Codec<PackageDependency> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            DependencyType.CODEC.fieldOf("type").forGetter(PackageDependency::type),
            DependencySource.CODEC.optionalFieldOf("source", DependencySource.PACK).forGetter(PackageDependency::source),
            Codec.STRING.fieldOf("id").forGetter(PackageDependency::id),
            ImmutableDependency.VERSION_RANGE_CODEC.optionalFieldOf("versionRange")
                .forGetter(PackageDependency::versionRange),
            Codec.STRING.optionalFieldOf("reason").forGetter(PackageDependency::reason),
            LoadOrdering.CODEC.optionalFieldOf("ordering").forGetter(PackageDependency::ordering)
        ).apply(builder, ImmutableDependency::new)
    );
}
