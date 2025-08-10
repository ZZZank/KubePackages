package zank.mods.kube_packages.bridge;

import dev.latvian.mods.kubejs.KubeJSPaths;
import zank.mods.kube_packages.KubePackages;

import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class KubePackagePaths {
    public static final Path PACKAGES = KubeJSPaths.dir(KubeJSPaths.DIRECTORY.resolve(KubePackages.MOD_ID));
    public static final Path CUSTOM = PACKAGES.resolve("custom");
    public static final Path INSTALLED = PACKAGES.resolve("installed");
}
