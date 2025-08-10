package zank.mods.kube_packages.impl.zip;

import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import zank.mods.kube_packages.api.KubePackageUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

public class ZipKubePackageProvider implements KubePackageProvider {
    private final Path basePath;

    public ZipKubePackageProvider(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        try (var stream = Files.list(basePath)) {
            var found = stream.filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(f -> f.getName().endsWith(".zip"))
                .map(this::safelyScanSingle)
                .filter(Objects::nonNull)
                .toList();
            KubePackages.LOGGER.info("Found {} packages from ZipKubePackageProvider", found.size());
            return found;
        } catch (IOException e) {
            KubePackages.LOGGER.error("Error when scanning zip file for ContentPack, ignoring all zip");
            return List.of();
        }
    }

    private KubePackage safelyScanSingle(File file) {
        try (var zipFile = new ZipFile(file)) {
            var entry = zipFile.getEntry(KubePackages.META_DATA_FILE_NAME);
            if (entry == null) {
                return null;
            } else if (entry.isDirectory()) {
                throw new RuntimeException(String.format(
                    "%s should be a file, but got a directory",
                    KubePackages.META_DATA_FILE_NAME
                ));
            }
            var metadata = KubePackageUtils.loadMetaDataOrThrow(zipFile.getInputStream(entry));
            return new ZipKubePackage(file.toPath(), metadata);
        } catch (Exception e) {
            KubePackages.LOGGER.error("Error when scanning zip file: {}", file.getName(), e);
            return null;
        }
    }
}
