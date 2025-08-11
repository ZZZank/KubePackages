package zank.mods.kube_packages.bridge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.EnumArgument;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import zank.mods.kube_packages.KubePackages;
import zank.mods.kube_packages.api.meta.PackageMetaData;
import zank.mods.kube_packages.bridge.kubejs.PackageExporter;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
@Mod.EventBusSubscriber
public class ForgeEventHandlers {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        var spOrOp =
            (Predicate<CommandSourceStack>) source -> source.getServer().isSingleplayer() || source.hasPermission(
                Commands.LEVEL_GAMEMASTERS);

        var dispatcher = event.getDispatcher();

        var command = Commands.literal("kpkg")
            .then(Commands.literal("export")
                .requires(spOrOp)
                .then(Commands.argument("id", StringArgumentType.string())
                    .then(Commands.argument("version", StringArgumentType.string())
                        .then(Commands.argument("exportAs", EnumArgument.enumArgument(PackageExporter.ExportType.class))
                            .executes(ForgeEventHandlers::exportPackage))))
            );
        dispatcher.register(command);
    }

    private static int exportPackage(CommandContext<CommandSourceStack> cx) throws CommandSyntaxException {
        KubePackages.LOGGER.info("Package export requested through command");

        var id = cx.getArgument("id", String.class);
        var version = cx.getArgument("version", String.class);
        var exportAs = cx.getArgument("exportAs", PackageExporter.ExportType.class);

        Consumer<Component> reporter = cx.getSource().getPlayerOrException()::sendSystemMessage;
        new PackageExporter(c -> reporter.accept(Component.literal("[KubePackages]").append(c)))
            .debugMode(true)
            .metadata(PackageMetaData.minimal(id, new DefaultArtifactVersion(version)))
            .exportAs(exportAs)
            .runAsync();

        KubePackages.LOGGER.info("Package export triggered through command");

        return Command.SINGLE_SUCCESS;
    }
}
