package zank.mods.kube_packages.impl.mod;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.forgespi.language.IModFileInfo;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.impl.KubePackageBase;
import dev.latvian.mods.kubejs.script.*;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class ModFileKubePackage extends KubePackageBase {
    private final IModFileInfo mod;

    public ModFileKubePackage(IModFileInfo mod, PackageMetaData metaData) {
        super(metaData);
        this.mod = mod;
    }

    @Override
    @Nullable
    protected ScriptPack createPack(ScriptLoadContext context) {
        var pack = KubePackageUtils.createEmptyPack(context, id());
        var basePath = this.mod.getFile().findResource(context.folderName());
        if (!Files.exists(basePath)) {
            return null;
        }

        try {
            Files.walk(basePath)
                .filter(Files::isRegularFile)
                .filter(p -> p.endsWith(".js"))
                .map(basePath::relativize)
                .forEach(path -> {
                    var fileInfo = new ScriptFileInfo(pack.info, path.toString());
                    var scriptSource = (ScriptSource.FromPath) info -> path;
                    context.loadFileIntoPack(pack, fileInfo, scriptSource);
                });
        } catch (IOException e) {
            KubePackages.LOGGER.error("Error when loading package from file", e);
        }
        return pack;
    }

    @Override
    public void getResource(PackType type, Consumer<Pack> packLoader) {
        // do nothing
    }

    @Override
    public String toString() {
        var modIds = mod.getMods()
            .stream()
            .map(IModInfo::getModId)
            .collect(Collectors.joining(","));
        return "ModFileKubePackage[mod=%s]".formatted(modIds);
    }
}
