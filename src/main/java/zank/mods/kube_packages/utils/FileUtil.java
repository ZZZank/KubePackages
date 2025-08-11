package zank.mods.kube_packages.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author ZZZank
 */
public class FileUtil {

    public static BufferedReader stream2reader(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }
}
