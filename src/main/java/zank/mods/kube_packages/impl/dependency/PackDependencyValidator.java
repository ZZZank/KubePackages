package zank.mods.kube_packages.impl.dependency;

import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import dev.architectury.platform.Platform;
import net.minecraft.network.chat.Component;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class PackDependencyValidator {
    private final DupeHandling duplicationHandling;
    private Map<String, KubePackage> named;

    public PackDependencyValidator(DupeHandling duplicationHandling) {
        this.duplicationHandling = Objects.requireNonNull(duplicationHandling);
    }

    public DependencyReport validate(Collection<KubePackage> packs) {
        var report = new DependencyReport();
        this.named = indexAndValidateId(packs, report);
        if (!report.errors().isEmpty() && duplicationHandling == DupeHandling.ERROR) {
            return report;
        }
        for (var pack : packs) {
            for (var dependency : pack.getMetaData().dependencies()) {
                validateSingle(pack, dependency, report);
            }
        }
        named = null;
        return report;
    }

    protected void validateSingle(KubePackage pack, PackageDependency dependency, DependencyReport report) {
        boolean targetPresent;
        ArtifactVersion targetVersion;
        switch (dependency.source()) {
            case PACK -> {
                var target = this.named.get(dependency.id());
                targetPresent = target != null;
                targetVersion = targetPresent
                    ? target.getMetaData().version().orElse(null)
                    : null;
            }
            case MOD -> {
                var target = Platform.getMod(dependency.id());
                targetPresent = target != null;
                targetVersion = targetPresent
                    ? new DefaultArtifactVersion(target.getVersion())
                    : null;
            }
            default -> throw new IllegalStateException("Unexpected dependency source: " + dependency.source());
        }

        var type = dependency.type();
        switch (type) {
            case REQUIRED, OPTIONAL, RECOMMENDED -> {
                Consumer<Component> reporter = switch (type) {
                    case REQUIRED -> report::addError;
                    case OPTIONAL -> report::addWarning;
                    case RECOMMENDED -> report::addInfo;
                    default -> throw new IllegalStateException();
                };
                if (!targetPresent) {
                    // required but not found
                    reporter.accept(dependency.toReport(pack).append(", but ContentPack with such id is not present"));
                } else if (dependency.versionRange().isPresent()) {
                    if (targetVersion == null) {
                        // specific version but no version
                        reporter.accept(dependency.toReport(pack).append(", but ContentPack with such id did not provide a version info"));
                    } else if (!dependency.versionRange().get().containsVersion(targetVersion)) {
                        // specific version but not matched
                        reporter.accept(dependency.toReport(pack).append(", but ContentPack with such id is at version '%s'".formatted(targetVersion)));
                    }
                }
            }
            case INCOMPATIBLE, DISCOURAGED -> {
                if (!targetPresent) {
                    return;
                }
                Consumer<Component> reporter = type == DependencyType.INCOMPATIBLE
                    ? report::addError
                    : report::addWarning;
                if (dependency.versionRange().isEmpty()) {
                    // any version not allowed
                    reporter.accept(dependency.toReport(pack)
                        .append(", but a package with such id exists"));
                } else if (targetVersion == null) {
                    // specific version, but got none
                    reporter.accept(dependency.toReport(pack)
                        .append(", but a package with such id did not provide version information"));
                } else if (dependency.versionRange().get().containsVersion(targetVersion)) {
                    // excluded version
                    reporter.accept(dependency.toReport(pack)
                        .append(", but a package with such id is at version '%s'".formatted(targetVersion)));
                }
            }
        }
    }

    private Map<String, KubePackage> indexAndValidateId(
        Collection<KubePackage> packs,
        DependencyReport report
    ) {
        var named = new HashMap<String, KubePackage>();
        for (var pack : packs) {
            var id = pack.id();
            var existed = named.get(id);

            if (existed == null) {
                named.put(id, pack);
                continue;
            }

            var error = Component.translatable(
                "%s and %s declared the same id '%s'",
                existed,
                pack,
                id
            );
            switch (this.duplicationHandling) {
                case ERROR -> report.addError(error);
                case PREFER_LAST -> {
                    named.put(id, pack);
                    KubePackages.LOGGER.warn(Component.empty()
                        .append(error)
                        .append(", overwriting old one")
                        .getString());
                }
                case PREFER_FIRST -> KubePackages.LOGGER.warn(Component.empty()
                    .append(error)
                    .append(", keeping old one")
                    .getString());
            }
        }
        return named;
    }

    public enum DupeHandling {
        ERROR,
        PREFER_FIRST,
        PREFER_LAST
    }
}
