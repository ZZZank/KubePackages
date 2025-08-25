package zank.mods.kube_packages.impl.dependency;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.slf4j.event.Level;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import dev.architectury.platform.Platform;
import net.minecraft.network.chat.Component;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.util.*;

/**
 * @author ZZZank
 */
@Getter
@Accessors(fluent = true)
public class PackDependencyValidator {
    private final DupeHandling dupeHandling;
    private final Map<String, KubePackage> indexed;
    private final DependencyReport report;
    private boolean used = false;

    public PackDependencyValidator(DupeHandling dupeHandling) {
        this.dupeHandling = Objects.requireNonNull(dupeHandling);
        indexed = new HashMap<>();
        report = new DependencyReport();
    }

    public void validate(Collection<? extends KubePackage> packs) {
        if (used) {
            throw new IllegalStateException("This validator instance has been used");
        }
        used = true;
        indexAndValidateId(packs, report);
        if (!report.getReportsAt(Level.ERROR).isEmpty() && dupeHandling == DupeHandling.ERROR) {
            return;
        }
        for (var pack : packs) {
            for (var dependency : pack.metadata().dependencies()) {
                validateSingleDependency(pack, dependency, report);
            }
        }
    }

    protected void validateSingleDependency(KubePackage pack, PackageDependency dependency, DependencyReport report) {
        boolean targetPresent;
        ArtifactVersion targetVersion;
        switch (dependency.source()) {
            case PACK -> {
                var target = this.indexed.get(dependency.id());
                targetPresent = target != null;
                targetVersion = targetPresent
                    ? target.metadata().version()
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
                if (!targetPresent) {
                    // required but not found
                    report.addReport(type.logLevel, dependency.toReport(pack)
                        .append(", but package with such id is not present"));
                } else if (dependency.versionRange().isPresent()) {
                    if (!dependency.versionRange().get().containsVersion(targetVersion)) {
                        // specific version but not matched
                        report.addReport(type.logLevel, dependency.toReport(pack)
                            .append(", but package with such id is at version '%s'".formatted(targetVersion)));
                    }
                }
            }
            case INCOMPATIBLE, DISCOURAGED -> {
                if (!targetPresent) {
                    return;
                }
                if (dependency.versionRange().isEmpty()) {
                    // any version not allowed
                    report.addReport(type.logLevel, dependency.toReport(pack)
                        .append(", but package with such id is missing"));
                } else if (dependency.versionRange().get().containsVersion(targetVersion)) {
                    // excluded version
                    report.addReport(type.logLevel, dependency.toReport(pack)
                        .append(", but package with such id is at version '%s'".formatted(targetVersion)));
                }
            }
        }
    }

    private void indexAndValidateId(Collection<? extends KubePackage> packs, DependencyReport report) {
        for (var pack : packs) {
            var id = pack.id();
            var existed = indexed.get(id);

            if (existed == null) {
                indexed.put(id, pack);
                continue;
            }

            handleDupedPackage(report, pack, existed);
        }
    }

    private void handleDupedPackage(DependencyReport report, KubePackage pack, KubePackage existed) {
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
                indexed.put(id, pack);
                report.addWarning(error.append(", overwriting the package found earlier"));
            }
            case PREFER_FIRST -> report.addWarning(error.append(", keeping the package found earlier"));
            case PREFER_NEWER -> {
                var version = pack.metadata().version();
                var existedVersion = existed.metadata().version();
                var existedComparedToFound = existedVersion.compareTo(version);
                if (existedComparedToFound == 0) {
                    report.addError(error);
                } else if (existedComparedToFound > 0) {// existed is newer
                    report.addWarning(error.append(", keeping the package found earlier"));
                } else { // < 0, existed is older
                    indexed.put(id, pack);
                    report.addWarning(error.append(", overwriting the package found earlier"));
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
