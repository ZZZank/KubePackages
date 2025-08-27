package zank.mods.kube_packages.impl.dummy;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import dev.latvian.mods.kubejs.script.ScriptPack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author ZZZank
 */
public class DummyKubePackage implements KubePackage {
    private final PackageMetadata metaData;
    private final Function<ScriptLoadContext, ScriptPack> toPack;

    public DummyKubePackage(PackageMetadata metaData, Function<ScriptLoadContext, ScriptPack> toPack) {
        this.metaData = metaData;
        this.toPack = toPack;
    }

    @Override
    public @Nullable ScriptPack getScript(ScriptLoadContext context) {
        return toPack.apply(context);
    }

    @Override
    public @Nullable PackResources getResource(PackType type) {
        return null;
    }

    @Override
    public PackageMetadata metadata() {
        return metaData;
    }

    @Override
    public String toString() {
        return "DummyKubePackage[%s]".formatted(metaData.id());
    }
}
