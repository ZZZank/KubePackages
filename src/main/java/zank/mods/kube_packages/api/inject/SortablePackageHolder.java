package zank.mods.kube_packages.api.inject;

import zank.mods.kube_packages.impl.dependency.SortableKubePackage;

import java.util.Map;

/**
 * @author ZZZank
 */
public interface SortablePackageHolder {

    Map<String, SortableKubePackage> kpkg$sortablePacks();
}
