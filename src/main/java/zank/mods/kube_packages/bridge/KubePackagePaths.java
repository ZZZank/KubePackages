package zank.mods.kube_packages.bridge;

import dev.architectury.platform.Platform;
import dev.latvian.mods.kubejs.KubeJSPaths;
import zank.mods.kube_packages.KubePackages;

import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class KubePackagePaths {
    public static final Path ROOT = KubeJSPaths.dir(Platform.getGameFolder().resolve(KubePackages.MOD_ID));
    public static final Path PACKAGES = ROOT.resolve("packages");
}
