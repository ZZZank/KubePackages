package zank.mods.kube_packages.impl;

import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.ScriptLoadContext;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author ZZZank
 */
public abstract class KubePackageBase implements KubePackage {
    protected final Map<ScriptType, Optional<ScriptPack>> packs = new EnumMap<>(ScriptType.class);
    protected final PackageMetadata metaData;

    protected KubePackageBase(PackageMetadata metaData) {
        this.metaData = metaData;
    }

    @Nullable
    protected abstract ScriptPack createPack(ScriptLoadContext context);

    @Override
    public @Nullable ScriptPack getScript(ScriptLoadContext context) {
        return this.packs.computeIfAbsent(
            context.type(),
            t -> Optional.ofNullable(createPack(context))
        ).orElse(null);
    }

    @Override
    public PackageMetadata metadata() {
        return metaData;
    }
}
