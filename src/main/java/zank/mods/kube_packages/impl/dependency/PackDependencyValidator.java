package zank.mods.kube_packages.impl.dependency;

import org.slf4j.event.Level;
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
    private final DupeHandling dupeHandling;
    private Map<String, KubePackage> named;

    public PackDependencyValidator(DupeHandling dupeHandling) {
        this.dupeHandling = Objects.requireNonNull(dupeHandling);
    }

    public DependencyReport validate(Collection<KubePackage> packs) {
        var report = new DependencyReport();
        this.named = indexAndValidateId(packs, report);
        if (!report.getReportsAt(Level.ERROR).isEmpty() && dupeHandling == DupeHandling.ERROR) {
            return report;
        }
        for (var pack : packs) {
            for (var dependency : pack.metaData().dependencies()) {
                validateSingleDependency(pack, dependency, report);
            }
        }
        named = null;
        return report;
    }

    protected void validateSingleDependency(KubePackage pack, PackageDependency dependency, DependencyReport report) {
        boolean targetPresent;
        ArtifactVersion targetVersion;
        switch (dependency.source()) {
            case PACK -> {
                var target = this.named.get(dependency.id());
                targetPresent = target != null;
                targetVersion = targetPresent
                    ? target.metaData().version()
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
                        reporter.accept(dependency.toReport(pack)
                            .append(", but ContentPack with such id did not provide a version info"));
                    } else if (!dependency.versionRange().get().containsVersion(targetVersion)) {
                        // specific version but not matched
                        reporter.accept(dependency.toReport(pack)
                            .append(", but ContentPack with such id is at version '%s'".formatted(targetVersion)));
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

            handleDupedPackage(report, pack, existed, named);
        }
        return named;
    }

    private void handleDupedPackage(
        DependencyReport report,
        KubePackage pack,
        KubePackage existed,
        Map<String, KubePackage> named
    ) {
        var id = existed.id();
        var error = Component.translatable(
            "%s and %s declared package with the same id '%s'",
            existed,
            pack,
            id
        );
        switch (this.dupeHandling) {
            case ERROR -> report.addError(error);
            case PREFER_LAST -> {
                named.put(id, pack);
                report.addWarning(error.append(", overwriting old one"));
            }
            case PREFER_FIRST -> report.addWarning(error.append(", keeping old one"));
            case PREFER_NEWER -> {
                var version = pack.metaData().version();
                var existedVersion = existed.metaData().version();
                var existedComparedToFound = existedVersion.compareTo(version);
                if (existedComparedToFound == 0) {
                    report.addError(error);
                } else if (existedComparedToFound > 0) {// existed is newer
                    report.addWarning(error.append(", keeping old one"));
                } else { // < 0, existed is older
                    named.put(id, pack);
                    report.addWarning(error.append(", overwriting old one"));
                }
            }
        }
    }

    public enum DupeHandling {
        ERROR,
        PREFER_FIRST,
        PREFER_LAST,
        PREFER_NEWER
    }
}
