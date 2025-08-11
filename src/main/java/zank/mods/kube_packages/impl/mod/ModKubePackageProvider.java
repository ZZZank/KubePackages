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
    private List<KubePackage> cached;

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        if (cached != null) {
            return cached;
        }

        cached = new ArrayList<>();
        for (var modFile : ModList.get().getModFiles()) {
            var metadata = findMetadata(modFile);
            if (metadata == null) {
                continue;
            }
            cached.add(new ModFileKubePackage(modFile, metadata));
        }
        return cached;
    }

    private static PackageMetaData findMetadata(IModFileInfo modFile) {
        var path = modFile.getFile().findResource(KubePackages.META_DATA_FILE_NAME);
        if (!Files.exists(path)) {
            return null;
        }
        try (var reader = Files.newBufferedReader(path)) {
            return KubePackageUtils.readMetaData(reader)
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
