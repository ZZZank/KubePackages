package zank.mods.kube_packages.impl.mod;

import net.minecraftforge.forgespi.language.IModFileInfo;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import zank.mods.kube_packages.api.KubePackageUtils;
import org.jetbrains.annotations.NotNull;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author ZZZank
 */
public class ModKubePackageProvider implements KubePackageProvider {
    private static final String METADATA_PATH =
        KubePackages.FOLDER_NAME + File.separator + KubePackages.META_DATA_FILE_NAME;

    public static boolean validate(IModFileInfo modFile) {
        var path = modFile.getFile().findResource(METADATA_PATH);
        return Files.exists(path) && Files.isRegularFile(path);
    }

    private final IModFileInfo mod;

    public ModKubePackageProvider(IModFileInfo mod) {
        this.mod = mod;
    }

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        try (var reader = Files.newBufferedReader(mod.getFile().findResource(METADATA_PATH))) {
            return KubePackageUtils.loadMetaData(reader)
                .resultOrPartial(error -> KubePackages.LOGGER.error(
                    "Error when parsing package metadata in mod '{}': {}",
                    GameUtil.extractModIds(mod),
                    error
                ))
                .map(meta -> List.of(new ModFileKubePackage(mod, meta)))
                .orElse(List.of());
        } catch (IOException e) {
            KubePackages.LOGGER.error(
                "Error when searching for ModContentPack in mod '{}'",
                GameUtil.extractModIds(mod),
                e
            );
            return List.of();
        }
    }

    @Override
    public String toString() {
        return "ModContentPackProvider[mod=%s]".formatted(GameUtil.extractModIds(mod));
    }
}
