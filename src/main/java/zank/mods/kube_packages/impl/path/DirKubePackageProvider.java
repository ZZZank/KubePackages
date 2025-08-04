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
        try {
            return Files.list(base)
                .filter(Files::isDirectory)
                .filter(DirKubePackage::validate)
                .map(DirKubePackage::new)
                .toList();
        } catch (IOException e) {
            KubePackages.LOGGER.error("Error when collecting package information from path", e);
            return List.of();
        }
    }
}
