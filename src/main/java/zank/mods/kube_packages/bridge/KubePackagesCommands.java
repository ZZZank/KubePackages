package zank.mods.kube_packages.bridge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.EnumArgument;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.event.Level;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.KubePackageUtils;
import zank.mods.kube_packages.api.meta.PackageMetadata;
import zank.mods.kube_packages.bridge.kubejs.MetadataBuilderJS;
import zank.mods.kube_packages.bridge.kubejs.PackageExporter;
import zank.mods.kube_packages.bridge.kubejs.PackageExporter.ExportType;
import zank.mods.kube_packages.impl.dependency.DependencyReport;
import zank.mods.kube_packages.utils.CodecUtil;
import zank.mods.kube_packages.utils.GameUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber
public class KubePackagesCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var spOrOp =
            (Predicate<CommandSourceStack>) source -> source.getServer().isSingleplayer()
                || source.hasPermission(Commands.LEVEL_GAMEMASTERS);

        var dispatcher = event.getDispatcher();

        var command = Commands.literal("kpkg")
            .then(Commands.literal("export")
                .requires(spOrOp)
                .then(Commands.argument("exportAs", EnumArgument.enumArgument(ExportType.class))
                    .then(Commands.literal("fileMetadata")
                        .then(Commands.argument("pathToMetadata", StringArgumentType.string())
                            .executes(cx -> {
                                PackageMetadata metadata;
                                var pathStr = cx.getArgument("pathToMetadata", String.class);
                                try (var reader = Files.newBufferedReader(GameUtil.resolveSafe(pathStr))) {
                                    metadata = KubePackageUtils.readMetadataOrThrow(reader);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return exportPackage(cx, metadata);
                            })))
                    .then(Commands.literal("cacheMetadata")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .suggests((cx, builder) -> SharedSuggestionProvider.suggest(
                                MetadataBuilderJS.COMMAND_CACHE.keySet(),
                                builder
                            ))
                            .executes(cx -> {
                                var id = cx.getArgument("id", String.class);
                                return exportPackage(cx, MetadataBuilderJS.COMMAND_CACHE.get(id));
                            })))
                    .then(Commands.literal("minimalMetadata")
                        .then(Commands.argument("id", StringArgumentType.string())
                            .then(Commands.argument("version", StringArgumentType.string())
                                .executes(cx -> {
                                    var id = cx.getArgument("id", String.class);
                                    var version = cx.getArgument("version", String.class);
                                    return exportPackage(
                                        cx,
                                        PackageMetadata.minimal(id, new DefaultArtifactVersion(version))
                                    );
                                }))))))
            .then(Commands.literal("package")
                .then(Commands.literal("list")
                    .executes(KubePackagesCommands::listPackages))
                .then(Commands.literal("show")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .executes(KubePackagesCommands::showPackage)))
                .then(Commands.literal("reload")
                    .executes(KubePackagesCommands::reloadPackages)));
        dispatcher.register(command);
    }

    private static Component prettyPrintMetadata(PackageMetadata metaData) {
        var tag = PackageMetadata.CODEC.encodeStart(NbtOps.INSTANCE, metaData).getOrThrow(true, CodecUtil.THROW_ERROR);
        return new TextComponentTagVisitor("  ", 0).visit(tag);
    }

    private static int exportPackage(CommandContext<CommandSourceStack> cx, PackageMetadata metadata)
        throws CommandSyntaxException {
        var reporter = extractReporter(cx);
        KubePackages.LOGGER.info("Package export requested through command");

        var exportAs = cx.getArgument("exportAs", ExportType.class);

        new PackageExporter(c -> reporter.accept(Component.empty().append(Component.literal("[KubePackages] ").kjs$blue()).append(c)))
            .debugMode(true)
            .metadata(metadata)
            .exportAs(exportAs)
            .runAsync();

        KubePackages.LOGGER.info("Package export triggered through command");

        return Command.SINGLE_SUCCESS;
    }

    private static int listPackages(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        var reporter = extractReporter(cx);

        var packages = KubePackages.getPackages();
        reporter.accept(Component.translatable("Found %s packages: ", packages.size()));
        for (var pkg : packages.values()) {
            var metaData = pkg.metadata();
            reporter.accept(Component.empty()
                .append(Component.literal("- ").kjs$darkGray())
                .append(Component.translatable("%s(%s): %s", metaData.displayName(), metaData.id(), metaData.version())
                    .kjs$green())
                .kjs$clickSuggestCommand("/kpkg package show " + metaData.id())
                .kjs$hover(Component.empty()
                    .append(Component.literal(pkg.toString()).kjs$aqua())
                    .append("\n")
                    .append("Click for detailed info"))
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int showPackage(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        var reporter = extractReporter(cx);

        var targetId = cx.getArgument("id", String.class);

        var found = KubePackages.getPackages().get(targetId);
        if (found == null) {
            reporter.accept(Component.literal("Cannot find package with id: ")
                .append(Component.literal(targetId).kjs$green()));
        } else {
            var text = Component.literal("Found ")
                .append(found.toString())
                .append(": ")
                .append(prettyPrintMetadata(found.metadata()));
            reporter.accept(text);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int reloadPackages(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        KubePackages.clearPackages();

        var _reportHolder = new DependencyReport[1];
        KubePackages.getPackages(report -> _reportHolder[0] = report);
        var report = _reportHolder[0];

        var msgSender = extractReporter(cx);

        msgSender.accept(Component.translatable(
            "Collected %s packages with %s, %s and %s",
            Component.literal(String.valueOf(KubePackages.getPackages().size())).kjs$green(),
            Component.translatable("%s error(s)", report.getReportsAt(Level.ERROR).size()).kjs$red(),
            Component.translatable("%s warning(s)", report.getReportsAt(Level.WARN).size()).kjs$yellow(),
            Component.translatable("%s info(s)", report.getReportsAt(Level.INFO).size()).kjs$blue()
        ));
        return Command.SINGLE_SUCCESS;
    }

    private static Consumer<Component> extractReporter(CommandContext<CommandSourceStack> cx)
        throws CommandSyntaxException {
        return cx.getSource().getPlayerOrException()::sendSystemMessage;
    }
}
