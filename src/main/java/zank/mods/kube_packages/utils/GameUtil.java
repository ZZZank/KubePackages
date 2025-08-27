package zank.mods.kube_packages.utils;

import com.google.common.base.Suppliers;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.slf4j.event.Level;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
public class GameUtil {

    public static final Supplier<Integer> RESOURCE_PACK_VERSION =
        Suppliers.memoize(() -> SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES));

    public static final Supplier<Integer> DATA_PACK_VERSION =
        Suppliers.memoize(() -> SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));

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

    public static Path resolveSafe(String path) {
        return ensureInGameFolder(FMLPaths.GAMEDIR.get().resolve(path));
    }

    public static Path ensureInGameFolder(Path path) {
        if (!isInGameFolder(path)) {
            throw new IllegalArgumentException("path not in game folder");
        }
        return path;
    }

    public static boolean isInGameFolder(Path path) {
        return path.normalize().toAbsolutePath().startsWith(FMLPaths.GAMEDIR.get());
    }

    public static Optional<IModInfo> findModInfo(String modId) {
        return ModList.get()
            .getModContainerById(modId)
            .map(ModContainer::getModInfo);
    }

    public static IModInfo findModInfoOrThrow(String modId) {
        return findModInfo(modId).orElseThrow(() -> new IllegalArgumentException("Cannot find mod with id: " + modId));
    }

    public static ChatFormatting logColor(Level level) {
        return switch (level) {
            case ERROR -> ChatFormatting.RED;
            case WARN -> ChatFormatting.YELLOW;
            case INFO -> ChatFormatting.BLUE;
            case DEBUG, TRACE -> ChatFormatting.GRAY;
        };
    }

    public static String logKey(Level level) {
        return switch (level) {
            case ERROR -> "error";
            case WARN -> "warning";
            case INFO -> "info";
            case DEBUG -> "debug";
            case TRACE -> "trace";
        };
    }
}
