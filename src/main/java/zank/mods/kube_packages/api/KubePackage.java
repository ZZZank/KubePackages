package zank.mods.kube_packages.api;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import dev.latvian.mods.kubejs.script.ScriptPack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author ZZZank
 */
public interface KubePackage {

    PackageMetaData metaData();

    @Nullable
    ScriptPack getScript(ScriptLoadContext context);

    void getResource(PackType type, Consumer<Pack> packLoader);

    @NotNull
    default String id() {
        return this.metaData().id();
    }

    @NotNull
    default ScriptPack postProcess(ScriptLoadContext context, @NotNull ScriptPack pack) {
        pack.scripts.sort(null);
        return pack;
    }
}
