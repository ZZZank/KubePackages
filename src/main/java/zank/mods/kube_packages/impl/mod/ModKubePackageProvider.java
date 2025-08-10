package zank.mods.kube_packages.impl.mod;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import zank.mods.kube_packages.api.KubePackageUtils;
import org.jetbrains.annotations.NotNull;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author ZZZank
 */
public class ModKubePackageProvider implements KubePackageProvider {

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        var packages = new ArrayList<KubePackage>();
        for (var modFile : ModList.get().getModFiles()) {
            var metadata = findMetadata(modFile);
            if (metadata == null) {
                continue;
            }
            packages.add(new ModFileKubePackage(modFile, metadata));
        }
        return packages;
    }

    private static PackageMetaData findMetadata(IModFileInfo modFile) {
        var path = modFile.getFile().findResource(KubePackages.META_DATA_FILE_NAME);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return null;
        }
        try (var reader = Files.newBufferedReader(path)) {
            return KubePackageUtils.loadMetaData(reader)
                .resultOrPartial(error -> KubePackages.LOGGER.error(
                    "Error when parsing package metadata in mod '{}': {}",
                    GameUtil.extractModIds(modFile),
                    error
                ))
                .orElse(null);
        } catch (IOException e) {
            KubePackages.LOGGER.error("Error when loading metadata in mod file '{}'", modFile.getFile().getFileName(), e);
            return null;
        }
    }
}
