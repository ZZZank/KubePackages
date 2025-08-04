package zank.mods.kube_packages.utils;

import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class GameUtil {

    public static String extractModIds(IModFileInfo modFileInfo) {
        return modFileInfo.getMods()
            .stream()
            .map(IModInfo::getModId)
            .collect(Collectors.joining(","));
    }
}
