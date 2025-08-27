package zank.mods.kube_packages.impl.mod;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.language.IModFileInfo;
import org.jetbrains.annotations.Nullable;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.impl.zip.ZipKubePackage;
import zank.mods.kube_packages.utils.GameUtil;

/**
 * @author ZZZank
 */
public class ModFileKubePackage extends ZipKubePackage {
    private final IModFileInfo modFile;

    public ModFileKubePackage(IModFileInfo modFile, PackageMetadata metaData) {
        super(modFile.getFile().getFilePath(), metaData);
        this.modFile = modFile;
    }

    @Override
    public @Nullable PackResources getResource(PackType type) {
        return null; // mod resource will be scanned by Forge
    }

    @Override
    public String toString() {
        return "ModFileKubePackage[mod=%s]".formatted(GameUtil.extractModIds(modFile));
    }
}
