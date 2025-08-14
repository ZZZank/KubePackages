package zank.mods.kube_packages.api.meta.dependency;

import com.mojang.serialization.Codec;
import org.slf4j.event.Level;
import zank.mods.kube_packages.utils.CodecUtil;

/**
 * @author ZZZank
 */
public enum DependencyType {
    /**
     * 需要才能运行的依赖，否则崩溃。
     */
    REQUIRED(Level.ERROR),
    /**
     * 不必要就能运行的依赖，不存在时不会崩溃，但是仍然参与依赖排序。
     */
    OPTIONAL(Level.INFO),
    /**
     * 不需要就能运行的依赖，用作元数据。
     */
    RECOMMENDED(Level.WARN),
    /**
     * 一起运行时可能出现问题的模组。一起运行时，输出警告。
     */
    DISCOURAGED(Level.WARN),
    /**
     * 一起运行可能导致崩溃。发现时打断加载。
     */
    INCOMPATIBLE(Level.ERROR),
    ;

    public static final Codec<DependencyType> CODEC = CodecUtil.createEnumStringCodec(DependencyType.class);

    public final Level logLevel;

    DependencyType(Level logLevel) {
        this.logLevel = logLevel;
    }
}
