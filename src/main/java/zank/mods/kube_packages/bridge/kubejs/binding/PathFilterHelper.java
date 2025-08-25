package zank.mods.kube_packages.bridge.kubejs.binding;

import dev.latvian.mods.kubejs.typings.Info;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SymbolicLinkFileFilter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author ZZZank
 */
public class PathFilterHelper {
    public static final Map<IOCase, PathFilterHelper> BY_CASE;
    public static final PathFilterHelper DEFAULT;

    static  {
        var map = new EnumMap<IOCase, PathFilterHelper>(IOCase.class);
        for (var ioCase : IOCase.values()) {
            map.put(ioCase, new PathFilterHelper(ioCase));
        }
        BY_CASE = Collections.unmodifiableMap(map);
        DEFAULT = BY_CASE.get(IOCase.SENSITIVE);
    }

    private final IOCase ioCase;

    public PathFilterHelper(IOCase ioCase) {
        this.ioCase = ioCase;
    }

    @Info("case sensitivity will affect filters matching file name/suffix/prefix/...")
    public IOCase getCurrentCaseSensitivity() {
        return ioCase;
    }

    @Info("get a new filter helper with specified case sensitivity")
    public PathFilterHelper withCaseSensitivity(IOCase caseSensitivity) {
        return BY_CASE.get(caseSensitivity);
    }

    public IOFileFilter prefix(final String prefix) {
        return FileFilterUtils.prefixFileFilter(prefix, ioCase);
    }

    public IOFileFilter suffix(final String suffix) {
        return FileFilterUtils.suffixFileFilter(suffix, ioCase);
    }

    public IOFileFilter fileNameOneOf(String... names) {
        return new NameFileFilter(names, ioCase);
    }

    public IOFileFilter fileNameNoneOf(String... names) {
        return not(fileNameOneOf(names));
    }

    @Info("""
        Accept all files""")
    public IOFileFilter always() {
        return FileFilterUtils.trueFileFilter();
    }

    @Info("""
        Deny all files""")
    public IOFileFilter never() {
        return FileFilterUtils.falseFileFilter();
    }

    public IOFileFilter isFile() {
        return FileFilterUtils.fileFileFilter();
    }

    public IOFileFilter and(IOFileFilter... filters) {
        return FileFilterUtils.and(filters);
    }

    public IOFileFilter or(IOFileFilter... filters) {
        return FileFilterUtils.or(filters);
    }

    public IOFileFilter not(IOFileFilter filter) {
        return FileFilterUtils.notFileFilter(filter);
    }

    public IOFileFilter symbolLink() {
        return SymbolicLinkFileFilter.INSTANCE;
    }

    public IOFileFilter notSymbolLink() {
        return not(symbolLink());
    }
}
