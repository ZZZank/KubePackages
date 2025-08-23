package zank.mods.kube_packages.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author ZZZank
 */
public class FileUtil {

    public static BufferedReader stream2reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    static Path resolve(final Path base, final Path relative) {
        var fileSystemTarget = base.getFileSystem();
        var fileSystemSource = relative.getFileSystem();
        if (fileSystemTarget == fileSystemSource) {
            return base.resolve(relative);
        }
        var separatorSource = fileSystemSource.getSeparator();
        var separatorTarget = fileSystemTarget.getSeparator();
        var otherString = relative.toString();
        return base.resolve(Objects.equals(separatorSource, separatorTarget)
            ? otherString
            : otherString.replace(separatorSource, separatorTarget)
        );
    }
}
