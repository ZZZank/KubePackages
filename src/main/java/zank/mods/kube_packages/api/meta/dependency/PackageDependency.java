package zank.mods.kube_packages.api.meta.dependency;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.maven.artifact.versioning.VersionRange;
import zank.mods.kube_packages.utils.CodecUtil;

import java.util.Optional;

/**
 * @author ZZZank
 */
public interface PackageDependency {

    static DependencyBuilder builder() {
        return new DependencyBuilder();
    }

    @NotNull
    DependencyType type();

    @NotNull
    DependencySource source();

    @NotNull
    String id();

    @NotNull
    Optional<VersionRange> versionRange();

    @NotNull
    Optional<String> reason();

    @NotNull
    Optional<LoadOrdering> ordering();

    default MutableComponent toReport(KubePackage parent) {
        return Component.translatable(
            "%s declared %s %s dependency '%s' at version range '%s'%s",
            Component.literal(parent.id()).withStyle(ChatFormatting.GREEN, ChatFormatting.UNDERLINE),
            Component.literal(this.type().toString()),
            Component.literal(this.source().toString()),
            Component.literal(this.id()).withStyle(ChatFormatting.GREEN),
            Component.literal(this.versionRange().map(VersionRange::toString).orElse("*"))
                .withStyle(ChatFormatting.GREEN),
            reason().isPresent() ? Component.translatable(", for reason '%s'", Component.literal(reason().get()).withStyle(ChatFormatting.GREEN)) : Component.empty()
        );
    }

    Codec<PackageDependency> CODEC = RecordCodecBuilder.create(
        builder -> builder.group(
            DependencyType.CODEC.fieldOf("type").forGetter(PackageDependency::type),
            DependencySource.CODEC.optionalFieldOf("source", DependencySource.PACK).forGetter(PackageDependency::source),
            Codec.STRING.fieldOf("id").forGetter(PackageDependency::id),
            CodecUtil.VERSION_RANGE_CODEC.optionalFieldOf("versionRange")
                .forGetter(PackageDependency::versionRange),
            Codec.STRING.optionalFieldOf("reason").forGetter(PackageDependency::reason),
            LoadOrdering.CODEC.optionalFieldOf("ordering").forGetter(PackageDependency::ordering)
        ).apply(builder, ImmutableDependency::new)
    );
}
