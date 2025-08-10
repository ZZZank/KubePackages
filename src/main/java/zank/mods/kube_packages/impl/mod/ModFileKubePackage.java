package zank.mods.kube_packages.impl.mod;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.forgespi.language.IModFileInfo;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.impl.zip.ZipKubePackage;
import zank.mods.kube_packages.utils.GameUtil;

import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public class ModFileKubePackage extends ZipKubePackage {
    private final IModFileInfo modFile;

    public ModFileKubePackage(IModFileInfo modFile, PackageMetaData metaData) {
        super(modFile.getFile().getFilePath(), metaData);
        this.modFile = modFile;
    }

    @Override
    public void getResource(PackType type, Consumer<Pack> packLoader) {
        // do nothing
    }

    @Override
    public String toString() {
        return "ModFileKubePackage[mod=%s]".formatted(GameUtil.extractModIds(modFile));
    }
}
