package zank.mods.kube_packages.utils;

import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class GameUtil {

    public static String toFolderName(ScriptType scriptType) {
        return scriptType.name + "_scripts";
    }

    public static String extractModIds(IModFileInfo modFileInfo) {
        return modFileInfo.getMods()
            .stream()
            .map(IModInfo::getModId)
            .collect(Collectors.joining(","));
    }

    public static VersionRange versionRangeFromSpecOrThrow(String spec) {
        try {
            return VersionRange.createFromVersionSpec(spec);
        } catch (InvalidVersionSpecificationException e) {
            throw new RuntimeException(e);
        }
    }
}
