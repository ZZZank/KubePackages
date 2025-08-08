package zank.mods.kube_packages.impl.dummy;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import dev.latvian.mods.kubejs.script.ScriptPack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public class DummyKubePackage implements KubePackage {
    private final PackageMetaData metaData;
    private final Function<ScriptLoadContext, ScriptPack> toPack;

    public DummyKubePackage(String namespace, Function<ScriptLoadContext, ScriptPack> toPack) {
        this(PackageMetaData.minimal(namespace), toPack);
    }

    public DummyKubePackage(PackageMetaData metaData, Function<ScriptLoadContext, ScriptPack> toPack) {
        this.metaData = metaData;
        this.toPack = toPack;
    }

    @Override
    public @Nullable ScriptPack getScript(ScriptLoadContext context) {
        return toPack.apply(context);
    }

    @Override
    public void getResource(PackType type, Consumer<Pack> packLoader) {
    }

    @Override
    public PackageMetaData getMetaData() {
        return metaData;
    }
}
