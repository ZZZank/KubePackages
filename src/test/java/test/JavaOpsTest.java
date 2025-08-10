package test;

import com.google.common.base.Suppliers;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;
import zank.mods.kube_packages.utils.GameUtil;
import zank.mods.kube_packages.utils.JavaOps;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public class JavaOpsTest {

    private static final Supplier<PackageMetaData> META_DATA = Suppliers.memoize(
        () -> new ImmutableMetaData(
            "examplemod",
            Optional.of("Exampl"),
            Optional.of("""
                wow description"""),
            new DefaultArtifactVersion("2.3.4"),
            Optional.empty(),
            List.of(),
            List.of(
                new ImmutableDependency(
                    DependencyType.REQUIRED,
                    DependencySource.MOD,
                    "forge",
                    Optional.of(GameUtil.versionRangeFromSpecOrThrow("[47,)")),
                    Optional.of("forge the modloader, you obvisiously need it"),
                    Optional.empty()
                )
            )
        )
    );

    @Test
    public void test() {
        var meta = META_DATA.get();
        var decoded = PackageMetaData.CODEC.encodeStart(JavaOps.INSTANCE, meta);
        var decodedMetadata = (Map<?, ?>) decoded
            .resultOrPartial(System.err::println)
            .orElseThrow();
        var decodedDependencies = (List<?>) decodedMetadata.get("dependencies");
        Assertions.assertEquals(1, decodedDependencies.size());
    }
}
