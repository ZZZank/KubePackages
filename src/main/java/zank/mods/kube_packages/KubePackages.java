package zank.mods.kube_packages;

import com.google.gson.Gson;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import zank.mods.kube_packages.api.KubePackage;
import zank.mods.kube_packages.api.KubePackageProvider;
import zank.mods.kube_packages.impl.dependency.DependencyReport;
import zank.mods.kube_packages.impl.dependency.PackDependencyValidator;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public static void registerProvider(KubePackageProvider provider) {
        PROVIDERS.add(Objects.requireNonNull(provider));
    }

    public static List<KubePackageProvider> viewProviders() {
        return Collections.unmodifiableList(PROVIDERS);
    }

    public static Map<String, KubePackage> getPackages(BiConsumer<Level, Component> perReportConsumer) {
        Objects.requireNonNull(perReportConsumer);
        return getPackages(reports -> {
            for (var entry : reports.viewAllReports().entrySet()) {
                var level = entry.getKey();
                for (var text : entry.getValue()) {
                    perReportConsumer.accept(level, text);
                }
            }
        });
    }

    public static synchronized Map<String, KubePackage> getPackages(Consumer<DependencyReport> reportsConsumer) {
        if (cachedPackages == null) {
            var provided = PROVIDERS.stream()
                .map(KubePackageProvider::provide)
                .flatMap(Collection::stream)
                .toList();

            var validator = new PackDependencyValidator(KubePackagesConfig.DUPE_HANDLING.get());
            validator.validate(provided);
            var report = validator.report();
            reportsConsumer.accept(report);

            cachedPackages = Collections.unmodifiableMap(validator.indexed());
            LOGGER.info(
                "Collected {} packages with {} errors, {} warnings and {} infos: {}",
                cachedPackages.size(),
                report.getReportsAt(Level.ERROR).size(),
                report.getReportsAt(Level.WARN).size(),
                report.getReportsAt(Level.INFO).size(),
                cachedPackages.values()
            );
        }
        return cachedPackages;
    }

    public static Map<String, KubePackage> getPackages() {
        return getPackages((level, text) -> KubePackages.LOGGER.atLevel(level).log(text.getString()));
    }

    public static void clearPackages() {
        cachedPackages = null;
    }
}
