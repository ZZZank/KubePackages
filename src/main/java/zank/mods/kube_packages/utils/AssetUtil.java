package zank.mods.kube_packages.utils;

import com.google.common.base.Suppliers;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import zank.mods.kube_packages.api.KubePackage;

import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class AssetUtil {
    public static final Supplier<Integer> RESOURCE_PACK_VERSION =
        Suppliers.memoize(() -> SharedConstants.getCurrentVersion()
            .getPackVersion(PackType.CLIENT_RESOURCES));
    public static final Supplier<Integer> DATA_PACK_VERSION =
        Suppliers.memoize(() -> SharedConstants.getCurrentVersion()
            .getPackVersion(PackType.SERVER_DATA));

    public static Pack packForPackage(
        KubePackage kubePackage,
        Component title,
        Component description,
        PackType type,
        Pack.ResourcesSupplier resources
    ) {
        return Pack.create(
            kubePackage.id(),
            title,
            true,
            resources,
            new Pack.Info(
                description,
                DATA_PACK_VERSION.get(),
                RESOURCE_PACK_VERSION.get(),
                FeatureFlagSet.of(FeatureFlags.BUNDLE),
                false
            ),
            type,
            Pack.Position.BOTTOM,
            true,
            PackSource.DEFAULT
        );
    }
}
