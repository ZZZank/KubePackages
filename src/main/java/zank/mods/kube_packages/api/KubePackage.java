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

    PackageMetaData getMetaData();

    /**
     * 如果该 ContentPack 没有{@link ScriptLoadContext#type()} 对应的 {@link ScriptPack}，返回 {@code null}
     */
    @Nullable
    ScriptPack getScript(ScriptLoadContext context);

    void getResource(PackType type, Consumer<Pack> packLoader);

    @NotNull
    default String id() {
        return this.getMetaData().id();
    }

    @NotNull
    default ScriptPack postProcess(ScriptLoadContext context, @NotNull ScriptPack pack) {
        pack.scripts.sort(null);
        return pack;
    }
}
