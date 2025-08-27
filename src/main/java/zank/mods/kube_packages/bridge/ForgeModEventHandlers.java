package zank.mods.kube_packages.bridge;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.resource.DelegatingPackResources;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.utils.GameUtil;

import java.util.Objects;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber(modid = KubePackages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModEventHandlers {

    @SubscribeEvent
    public static void addPackRepo(AddPackFindersEvent event) {
        var type = event.getPackType();
        event.addRepositorySource(loader -> {
            var desc = Component.translatable("Resource for {0} packages", KubePackages.getPackages().size());
            var packResources = KubePackages.getPackages()
                .values()
                .stream()
                .map(p -> p.getResource(type))
                .filter(Objects::nonNull)
                .toList();

            var pack = Pack.readMetaAndCreate(
                "kube_packages_resources",
                Component.literal("KubePackages Resources"),
                true,
                id -> new DelegatingPackResources(
                    id,
                    false,
                    new PackMetadataSection(desc, GameUtil.RESOURCE_PACK_VERSION.get()),
                    packResources
                ),
                PackType.CLIENT_RESOURCES,
                Pack.Position.BOTTOM,
                PackSource.DEFAULT
            );
            loader.accept(pack);
        });
    }
}
