package zank.mods.kube_packages.api;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author ZZZank
 */
public interface KubePackageProvider {

    @NotNull
    Collection<? extends @NotNull KubePackage> provide();
}
