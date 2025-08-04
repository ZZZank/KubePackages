package zank.mods.kube_packages.api.meta.dependency;

import com.mojang.serialization.Codec;
import zank.mods.kube_packages.utils.CodecUtil;

/**
 * @author ZZZank
 */
public enum LoadOrdering {
    NONE,
    BEFORE,
    AFTER,
    ;

    public static final Codec<LoadOrdering> CODEC = CodecUtil.createEnumStringCodec(LoadOrdering.class);
}
