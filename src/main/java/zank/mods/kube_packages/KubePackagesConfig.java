package zank.mods.kube_packages;

import net.minecraftforge.common.ForgeConfigSpec;
import zank.mods.kube_packages.impl.dependency.PackDependencyValidator;

/**
 * @author ZZZank
 */
public class KubePackagesConfig {
    public static final ForgeConfigSpec INSTANCE;

    public static final ForgeConfigSpec.EnumValue<PackDependencyValidator.DupeHandling> DUPE_HANDLING;

    static {
        var builder = new ForgeConfigSpec.Builder();

        builder.comment("Loading and validating packages").push("loading");

        DUPE_HANDLING = builder.comment("Strategy when multiple packages with same id are found")
            .defineEnum("dupeHandling", PackDependencyValidator.DupeHandling.ERROR);

        builder.pop();

        INSTANCE = builder.build();
    }
}
