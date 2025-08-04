package zank.mods.kube_packages.bridge;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.common.Mod;
import zank.mods.kube_packages.KubePackages;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber(modid = KubePackages.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeModEventHandlers {

    public static void addPackRepo(AddPackFindersEvent event) {

    }
}
