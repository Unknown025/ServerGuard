package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.List;

public class CommandKill extends PermissionCommandBase {
    @Override
    public String getCommandName() {
        return "kill";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getPermissionNode() {
        return "minecraft.command.kill";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.kill.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = null;
        if (args.length == 0) {
            player = getCommandSenderAsPlayer(sender);
        } else if (args.length == 1) {
            player = getPlayer(sender, args[0]);
        }
        if (player == null) {
            throw new PlayerNotFoundException();
        }
        player.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
        if (player == sender) {
            sender.addChatMessage(new ChatComponentTranslation("commands.kill.success"));
        } else {
            notifyOperators(sender, this, "commands.kill.success.other", player.getCommandSenderName());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
