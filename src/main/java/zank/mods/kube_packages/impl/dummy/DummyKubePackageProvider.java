package zank.mods.kube_packages.impl.dummy;

import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author ZZZank
 */
public class DummyKubePackageProvider implements KubePackageProvider {
    @NotNull
    private final Collection<? extends @NotNull KubePackage> packs;

    public DummyKubePackageProvider(@NotNull Collection<? extends @NotNull KubePackage> packs) {
        this.packs = packs;
    }

    @Override
    public @NotNull Collection<? extends @NotNull KubePackage> provide() {
        return packs;
    }
}
