package zank.mods.kube_packages.bridge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.EnumArgument;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.bridge.kubejs.PackageExporter;
import zank.mods.kube_packages.utils.CodecUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber
public class KubePackagesCommands {

    private static final Component LINE_BREAK = Component.literal("\n");

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var spOrOp =
            (Predicate<CommandSourceStack>) source -> source.getServer().isSingleplayer()
                || source.hasPermission(Commands.LEVEL_GAMEMASTERS);

        var dispatcher = event.getDispatcher();

        var command = Commands.literal("kpkg")
            .then(Commands.literal("export")
                .requires(spOrOp)
                .then(Commands.argument("id", StringArgumentType.string())
                    .then(Commands.argument("version", StringArgumentType.string())
                        .then(Commands.argument("exportAs", EnumArgument.enumArgument(PackageExporter.ExportType.class))
                            .executes(KubePackagesCommands::exportPackage))))
            )
            .then(Commands.literal("package")
                .then(Commands.literal("list")
                    .executes(KubePackagesCommands::listPackages))
                .then(Commands.literal("findInstalled")
                    .then(Commands.argument("id", StringArgumentType.string())
                        .executes(KubePackagesCommands::showPackage))));
        dispatcher.register(command);
    }

    private static Component prettyPrintMetadata(PackageMetaData metaData) {
        var tag = PackageMetaData.CODEC.encodeStart(NbtOps.INSTANCE, metaData)
            .getOrThrow(true, CodecUtil.THROW_ERROR);
        return new TextComponentTagVisitor("  ", 0).visit(tag);
    }

    private static int exportPackage(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        var reporter = extractReporter(cx);
        KubePackages.LOGGER.info("Package export requested through command");

        var id = cx.getArgument("id", String.class);
        var version = cx.getArgument("version", String.class);
        var exportAs = cx.getArgument("exportAs", PackageExporter.ExportType.class);

        new PackageExporter(c -> reporter.accept(Component.literal("[KubePackages] ").kjs$blue().append(c)))
            .debugMode(true)
            .metadata(PackageMetaData.minimal(id, new DefaultArtifactVersion(version)))
            .exportAs(exportAs)
            .runAsync();

        KubePackages.LOGGER.info("Package export triggered through command");

        return Command.SINGLE_SUCCESS;
    }

    private static int listPackages(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        var reporter = extractReporter(cx);

        var packages = KubePackages.getPackages();
        reporter.accept(Component.translatable("Found %s packages:", packages.size()));
        for (var pkg : packages) {
            var metaData = pkg.getMetaData();
            reporter.accept(
                Component.empty()
                    .append(Component.literal("- ").kjs$darkGray())
                    .append(Component.translatable(
                        "%s(%s): %s",
                        metaData.displayName(),
                        metaData.id(),
                        metaData.version()
                    ).kjs$green())
                    .kjs$clickSuggestCommand("/kpkg package findInstalled " + metaData.id())
                    .kjs$hover(Component.literal("Click for detailed info"))
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int showPackage(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        var reporter = extractReporter(cx);

        var targetId = cx.getArgument("id", String.class);

        var text = KubePackages.getPackages()
            .stream()
            .filter(p -> p.id().equals(targetId))
            .findFirst()
            .map(pkg -> Component.literal("Found ")
                .append(pkg.toString())
                .append(": ")
                .append(prettyPrintMetadata(pkg.getMetaData())))
            .orElse(Component.literal("Cannot find package with id: ").append(targetId));
        reporter.accept(text);

        return Command.SINGLE_SUCCESS;
    }

    private static Consumer<Component> extractReporter(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        return cx.getSource().getPlayerOrException()::sendSystemMessage;
    }
}
