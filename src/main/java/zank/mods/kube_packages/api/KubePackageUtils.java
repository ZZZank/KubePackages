package zank.mods.kube_packages.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.dependency.DependencySource;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.api.meta.dependency.DependencyType;
import zank.mods.kube_packages.impl.dependency.ImmutableDependency;
import zank.mods.kube_packages.api.meta.dependency.PackageDependency;
import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.kubejs.script.ScriptPackInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * @author ZZZank
 */
public class KubePackageUtils {
    public static ScriptPack createEmptyPack(ScriptLoadContext context, String id) {
        return new ScriptPack(context.manager(), new ScriptPackInfo(id, ""));
    }

    public static DataResult<PackageMetaData> loadMetaData(Reader reader) {
        var json = KubePackages.GSON.fromJson(reader, JsonObject.class);
        return PackageMetaData.CODEC.parse(JsonOps.INSTANCE, json);
    }

    public static DataResult<PackageMetaData> loadMetaData(InputStream stream) {
        try (var reader = FileUtil.stream2reader(stream)) {
            return loadMetaData(reader);
        } catch (IOException e) {
            return DataResult.error(e::toString);
        }
    }

    public static PackageMetaData loadMetaDataOrThrow(InputStream stream) {
        return loadMetaData(stream)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }

    public static PackageMetaData loadMetaDataOrThrow(Reader reader) {
        return loadMetaData(reader)
            .resultOrPartial(CodecUtil.THROW_ERROR)
            .orElseThrow();
    }

    public static PackageMetaData metadataFromMod(IModInfo mod) {
        return new ImmutableMetaData(
            mod.getModId(),
            Optional.of(mod.getDisplayName()),
            Optional.of(mod.getDescription()),
            mod.getVersion(),
            Optional.of(mod.getOwningFile().getLicense()).map(s -> s.isEmpty() ? null : s),
            List.of(),
            mod.getDependencies()
                .stream()
                .map(KubePackageUtils::dependencyFromMod)
                .toList()
        );
    }

    public static PackageDependency dependencyFromMod(IModInfo.ModVersion modDep) {
        return new ImmutableDependency(
            modDep.isMandatory() ? DependencyType.REQUIRED : DependencyType.OPTIONAL,
            DependencySource.MOD,
            modDep.getModId(),
            Optional.of(modDep.getVersionRange()),
            modDep.getReferralURL().map(URL::toString),
            Optional.empty()
        );
    }
}
