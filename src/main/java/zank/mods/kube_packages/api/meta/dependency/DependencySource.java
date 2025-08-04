package zank.mods.kube_packages.api.meta.dependency;

import com.mojang.serialization.Codec;
import zank.mods.kube_packages.utils.CodecUtil;

/**
 * @author ZZZank
 */
public enum DependencySource {
    PACK,
    MOD
    ;

    public static final Codec<DependencySource> CODEC = CodecUtil.createEnumStringCodec(DependencySource.class);
}
