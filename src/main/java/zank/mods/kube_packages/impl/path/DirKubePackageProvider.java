package zank.mods.kube_packages.impl.path;

import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author ZZZank
 */
public class DirKubePackageProvider implements KubePackageProvider {
    private final Path base;

    public DirKubePackageProvider(Path base) {
        this.base = base;
    }

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        try (var stream = Files.list(base)) {
            return stream.filter(Files::isDirectory)
                .map(DirKubePackage::tryLoad)
                .filter(Objects::nonNull)
                .toList();
        } catch (IOException e) {
            KubePackages.LOGGER.error("Error when collecting package information from path", e);
            return List.of();
        }
    }
}
