package zank.mods.kube_packages.bridge;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import zank.mods.kube_packages.KubePackages;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber(modid = KubePackages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModEventHandlers {

    @SubscribeEvent
    public static void addPackRepo(AddPackFindersEvent event) {
        var type = event.getPackType();
        event.addRepositorySource(loader -> KubePackages.getPackages().forEach((id, p) -> p.getResource(type, loader)));
    }
}
