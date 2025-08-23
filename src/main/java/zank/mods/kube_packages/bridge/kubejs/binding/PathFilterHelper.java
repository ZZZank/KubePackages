package zank.mods.kube_packages.bridge.kubejs.binding;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.SymbolicLinkFileFilter;

/**
 * @author ZZZank
 */
public class PathFilterHelper {
    public static final PathFilterHelper INSTANCE = new PathFilterHelper();

    public IOFileFilter prefix(final String prefix, final IOCase ioCase) {
        return FileFilterUtils.prefixFileFilter(prefix, ioCase);
    }

    public IOFileFilter prefix(final String prefix) {
        return FileFilterUtils.prefixFileFilter(prefix);
    }

    public IOFileFilter suffix(final String suffix, final IOCase ioCase) {
        return FileFilterUtils.suffixFileFilter(suffix, ioCase);
    }

    public IOFileFilter suffix(final String suffix) {
        return FileFilterUtils.suffixFileFilter(suffix);
    }

    public IOFileFilter fileNameOneOf(String... names) {
        return new NameFileFilter(names);
    }

    public IOFileFilter fileNameOneOf(IOCase ioCase, String... names) {
        return new NameFileFilter(names, ioCase);
    }

    public IOFileFilter fileNameNoneOf(String... names) {
        return not(fileNameOneOf(names));
    }

    public IOFileFilter fileNameNoneOf(IOCase ioCase, String... names) {
        return not(fileNameOneOf(ioCase, names));
    }

    public IOFileFilter always() {
        return FileFilterUtils.trueFileFilter();
    }

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
