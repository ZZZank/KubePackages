package zank.mods.kube_packages;

import com.google.gson.Gson;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import zank.mods.kube_packages.impl.dependency.DependencyReport;
import zank.mods.kube_packages.impl.dependency.PackDependencyValidator;

import java.util.*;

/**
 * @author ZZZank
 */
@Mod(KubePackages.MOD_ID)
public class KubePackages {
    public static final String MOD_ID = "kube_packages";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String META_DATA_FILE_NAME = "kube_package.json";
    public static final Gson GSON = new Gson();

    public KubePackages() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KubePackagesConfig.INSTANCE);
    }

    private static final List<KubePackageProvider> PROVIDERS = new ArrayList<>();
    private static volatile Map<String, KubePackage> cachedPackages = null;
    private static DependencyReport cachedReport = null;

    public static void registerProvider(KubePackageProvider provider) {
        PROVIDERS.add(Objects.requireNonNull(provider));
    }

    public static List<KubePackageProvider> viewProviders() {
        return Collections.unmodifiableList(PROVIDERS);
    }

    public static DependencyReport ensurePackagesLoaded() {
        if (cachedPackages != null) {
            return null;
        }
        var provided = PROVIDERS.stream()
            .map(KubePackageProvider::provide)
            .flatMap(Collection::stream)
            .toList();

        var validator = new PackDependencyValidator(PackDependencyValidator.DupeHandling.ERROR);
        validator.validate(provided);
        var report = validator.report();

        cachedPackages = Collections.unmodifiableMap(validator.indexed());
        cachedReport = report;
        return report;
    }

    public static Map<String, KubePackage> getPackages() {
        ensurePackagesLoaded();
        return cachedPackages;
    }

    public static DependencyReport getPackageLoadReport() {
        if (cachedPackages == null) {
            throw new IllegalStateException(".getPackageLoadReport() called before packages are collected via .getPackages()");
        }
        return cachedReport;
    }

    public static void clearPackages() {
        cachedPackages = null;
        cachedReport = null;
    }
}
