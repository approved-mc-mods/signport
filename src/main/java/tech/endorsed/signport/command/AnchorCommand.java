package tech.endorsed.signport.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import tech.endorsed.signport.world.Anchor;
import tech.endorsed.signport.world.AnchorState;

import static net.minecraft.server.command.CommandManager.literal;

public class AnchorCommand {
    private static final SimpleCommandExceptionType CREATE_FAILED_EXCEPTION
            = new SimpleCommandExceptionType(Text.translatable("commands.anchor.create.failed"));
    private static final SimpleCommandExceptionType NAME_CLASH_EXCEPTION
            = new SimpleCommandExceptionType(Text.translatable("commands.anchor.create.nameclash"));
    private static final SimpleCommandExceptionType UNKNOWN_NAME_EXCEPTION
            = new SimpleCommandExceptionType(Text.translatable("commands.anchor.delete.unknownname"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(
                literal("signport")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(literal("anchor")
                                .then(literal("list")
                                        .executes(context -> AnchorCommand.listAnchors(context.getSource())))
                                .then(literal("delete")
                                        .then(CommandManager.argument("name", StringArgumentType.word())
                                                .executes(context -> AnchorCommand.deleteAnchor(context.getSource(), StringArgumentType.getString(context, "name")))))
                                .then(literal("create")
                                        .then(CommandManager.argument("name", StringArgumentType.word())
                                                .executes(context -> AnchorCommand.createAnchor(context.getSource(), StringArgumentType.getString(context, "name"), null))
                                                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                                        .executes(context -> AnchorCommand.createAnchor(context.getSource(), StringArgumentType.getString(context, "name"), BlockPosArgumentType.getLoadedBlockPos(context, "pos"))))))));

        dispatcher.register(literal("sp")
                .requires(source -> source.hasPermissionLevel(2))
                .redirect(literalCommandNode));
    }

    public static int createAnchor(ServerCommandSource source, String name, BlockPos pos) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        AnchorState anchorState = AnchorState.getServerState(source.getWorld());
        if (anchorState == null) return 0;

        for (Anchor anchor: anchorState.GetAnchors()) {
            if (anchor.name.equals(name)) throw NAME_CLASH_EXCEPTION.create();
            if (anchor.pos.equals(pos)) throw CREATE_FAILED_EXCEPTION.create();
        }

        Anchor anchor = new Anchor();
        anchor.name = name;
        anchor.pos = pos == null ? source.getPlayer().getBlockPos() : pos;
        anchorState.anchors.add(anchor);
        anchorState.markDirty();

        player.sendMessage(Text.literal("Created anchor '%s'".formatted(name)));

        return 1;
    }

    public static int deleteAnchor(ServerCommandSource source, String name) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        AnchorState anchorState = AnchorState.getServerState(source.getWorld());
        if (anchorState == null) return 0;

        int i = 0;
        for (Anchor anchor: anchorState.GetAnchors()) {
            if (anchor.name.equals(name)) {
                anchorState.anchors.remove(i);
                anchorState.markDirty();
                player.sendMessage(Text.literal("Deleted anchor '%s'".formatted(name)));
                return 1;
            }
            i = i + 1;
        }

        if (name.equalsIgnoreCase("all")) {
            anchorState.anchors.clear();
            anchorState.markDirty();
            player.sendMessage(Text.literal("Deleted ALL anchors"));
            return 1;
        }

        throw UNKNOWN_NAME_EXCEPTION.create();
    }

    public static int listAnchors(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;

        AnchorState anchorState = AnchorState.getServerState(source.getWorld());
        if (anchorState == null) return 0;
        if (anchorState.GetAnchors().size() == 0) {
            player.sendMessage(Text.literal("No anchors exist"));
            return  1;
        }

        int i = 1;
        for (Anchor anchor: anchorState.GetAnchors()) {
            MutableText message = Text.literal("[%d] %s [%d, %d, %d]"
                    .formatted(i, anchor.name, anchor.pos.getX(), anchor.pos.getY(), anchor.pos.getZ()));

            if (player.hasPermissionLevel(2)) {
                message = message.setStyle(
                        message.getStyle().withClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp @s %d %d %d"
                                        .formatted(anchor.pos.getX(), anchor.pos.getY(), anchor.pos.getZ()))));
            }

            player.sendMessage(message);
            i = i + 1;
        }
        return i - 1;
    }
}
