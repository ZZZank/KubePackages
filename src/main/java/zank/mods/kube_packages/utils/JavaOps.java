package zank.mods.kube_packages.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class JavaOps implements DynamicOps<Object> {
    public static final JavaOps INSTANCE = new JavaOps();

    protected JavaOps() {}

    public static final Object EMPTY = new Object();

    @Override
    public Object empty() {
        return EMPTY;
    }

    @Override
    public Object emptyMap() {
        return Map.of();
    }

    @Override
    public Object emptyList() {
        return List.of();
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Object input) {
        if (input instanceof Map<?,?>) {
            return convertMap(outOps, input);
        } else if (input instanceof List<?>) {
            return convertList(outOps, input);
        } else if (input == EMPTY) {
            return outOps.empty();
        } else if (input instanceof String str) {
            return outOps.createString(str);
        } else if (input instanceof Boolean boo) {
            return outOps.createBoolean(boo);
        } else if (input instanceof Number num) {
            if (input instanceof Integer i) {
                return outOps.createInt(i);
            } else if (input instanceof Short s) {
                return outOps.createShort(s);
            } else if (input instanceof Long l) {
                return outOps.createLong(l);
            } else if (input instanceof Float f) {
                return outOps.createFloat(f);
            } else if (input instanceof Double d) {
                return outOps.createDouble(d);
            } else if (input instanceof Byte b) {
                return outOps.createByte(b);
            }
            return outOps.createNumeric(num);
        }
        return null;
    }

    @Override
    public DataResult<Number> getNumberValue(Object input) {
        return input instanceof Number num
            ? DataResult.success(num)
            : DataResult.error(() -> input + " is not a number");
    }

    @Override
    public Object createNumeric(Number i) {
        return i;
    }

    @Override
    public DataResult<String> getStringValue(Object input) {
        return input == null
            ? DataResult.error(() -> "null value")
            : DataResult.success(input.toString());
    }

    @Override
    public Object createString(String value) {
        return value;
    }

    @Override
    public Object createBoolean(boolean value) {
        return value;
    }

    @Override
    public DataResult<Object> mergeToList(Object list, Object value) {
        if (list == EMPTY) {
            return DataResult.success(List.of(value));
        } else if (list instanceof List<?> l) {
            var merged = new ArrayList<>(l.size() + 1);
            merged.addAll(l);
            merged.add(value);
            return DataResult.success(merged);
        }
        return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
    }

    @Override
    public DataResult<Object> mergeToList(Object list, List<Object> values) {
        if (list == EMPTY) {
            return DataResult.success(List.copyOf(values));
        } else if (list instanceof List<?> l) {
            var merged = new ArrayList<>(l.size() + 1);
            merged.addAll(l);
            merged.addAll(values);
            return DataResult.success(merged);
        }
        return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
        if (map == EMPTY) {
            return DataResult.success(Map.of(key, value));
        }
        if (!(map instanceof Map<?,?> m)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        if (!(key instanceof String)) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }

        var out = new HashMap<Object, Object>(m);
        out.put(key, value);
        return DataResult.success(out);
    }

    @Override
    public DataResult<Object> mergeToMap(Object map, MapLike<Object> values) {
        if (map == EMPTY) {
            return DataResult.success(createMap(values.entries()));
        }
        if (!(map instanceof Map<?,?> m)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        var out = new HashMap<Object, Object>(m);
        values.entries().forEach(p -> out.put(p.getFirst(), p.getSecond()));
        return DataResult.success(out);
    }

    @Override
    public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
        return input instanceof Map<?, ?> m
            ? DataResult.success(m.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())))
            : DataResult.error(() -> "Not a map: " + input);
    }

    @Override
    public Object createMap(Stream<Pair<Object, Object>> stream) {
        var map = new HashMap<>();
        stream.forEach(p -> map.put(p.getFirst(), p.getSecond()));
        return map;
    }

    @Override
    public DataResult<Stream<Object>> getStream(Object input) {
        return input instanceof Collection<?> c
            ? DataResult.success((Stream<Object>) c.stream())
            : DataResult.error(() -> "Not a collection: " + input);
    }

    @Override
    public Object createList(Stream<Object> input) {
        return input.toList();
    }

    @Override
    public Object remove(Object input, String key) {
        if (input instanceof Map<?,?> m) {
            var out = new HashMap<>(m);
            out.remove(key);
            return out;
        }
        return input;
    }
}
