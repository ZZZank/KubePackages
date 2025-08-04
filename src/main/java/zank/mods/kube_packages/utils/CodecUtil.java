package zank.mods.kube_packages.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class CodecUtil {
    public static <T extends Enum<T>> Codec<T> createEnumStringCodec(Class<T> type) {
        return createEnumStringCodec(type, true);
    }

    public static <T extends Enum<T>> Codec<T> createEnumStringCodec(final Class<T> type, final boolean ignoreCase) {
        Function<String, DataResult<T>> toEnum;
        if (ignoreCase) {
            var indexed = Arrays.stream(type.getEnumConstants())
                .collect(Collectors.toMap(
                    e -> e.name().toLowerCase(Locale.ROOT),
                    Function.identity()
                ));
            toEnum = name -> {
                if (name == null) {
                    throw new NullPointerException("Name is null");
                }
                var result = indexed.get(name.toLowerCase(Locale.ROOT));
                return result == null
                    ? DataResult.error(() -> "No enum constant " + type.getCanonicalName() + "." + name)
                    : DataResult.success(result);
            };
        } else {
            toEnum = (UnsafeFunction<String, T>) name -> Enum.valueOf(type, name);
        }
        return Codec.STRING.comapFlatMap(toEnum, Enum::name);
    }

    public static <I, O> Function<I, DataResult<O>> wrapUnsafeFn(UnsafeFunction<I, O> function) {
        return function;
    }

    public interface UnsafeFunction<I, O> extends Function<I, DataResult<O>> {
        O applyUnsafe(I input) throws Exception;

        @Override
        default DataResult<O> apply(I i) {
            try {
                return DataResult.success(applyUnsafe(i));
            } catch (Exception e) {
                return DataResult.error(e::toString);
            }
        }
    }
}
