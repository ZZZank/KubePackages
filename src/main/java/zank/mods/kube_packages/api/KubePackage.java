package zank.mods.kube_packages.api;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import dev.latvian.mods.kubejs.script.ScriptPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author ZZZank
 */
public interface KubePackage {

    PackageMetadata metadata();

    @Nullable
    ScriptPack getScript(ScriptLoadContext context);

    @Nullable
    PackResources getResource(PackType type);

    @NotNull
    default String id() {
        return this.metadata().id();
    }

    @NotNull
    default ScriptPack postProcess(ScriptLoadContext context, @NotNull ScriptPack pack) {
        pack.scripts.sort(null);
        return pack;
    }
}
