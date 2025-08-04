package zank.mods.kube_packages;

import com.google.gson.Gson;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ZZZank
 */
@Mod(KubePackages.MOD_ID)
public class KubePackages {
    public static final String MOD_ID = "kube_packages";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String FOLDER_NAME = "kube_packages";
    public static final String META_DATA_FILE_NAME = "kube_package.json";
    public static final Gson GSON = new Gson();

    private static final List<KubePackageProvider> PROVIDERS = new ArrayList<>();
    private static volatile List<KubePackage> cachedPackages = null;

    public static void registerProvider(KubePackageProvider provider) {
        PROVIDERS.add(Objects.requireNonNull(provider));
    }

    public static List<KubePackageProvider> viewProviders() {
        return Collections.unmodifiableList(PROVIDERS);
    }

    public static synchronized List<KubePackage> getPackages() {
        if (cachedPackages == null) {
            cachedPackages = PROVIDERS.stream()
                .map(KubePackageProvider::provide)
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableList());
        }
        return cachedPackages;
    }

    public static void clearPackages() {
        cachedPackages = null;
    }
}
