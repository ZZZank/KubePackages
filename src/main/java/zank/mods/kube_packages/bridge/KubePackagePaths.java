package zank.mods.kube_packages.bridge;

import dev.architectury.platform.Platform;
import zank.mods.kube_packages.KubePackages;

import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class KubePackagePaths {
    public static final Path GAME_ROOT = Platform.getGameFolder();
    public static final Path PACKAGES = GAME_ROOT.resolve(KubePackages.MOD_ID);
    public static final Path CUSTOM = PACKAGES.resolve("custom");
    public static final Path INSTALLED = PACKAGES.resolve("installed");
}
