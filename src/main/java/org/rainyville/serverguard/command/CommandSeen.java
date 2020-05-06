package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.util.List;

public class CommandSeen extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Checks how long ago someone was online.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.seen";
    }

    @Override
    public String getCommandName() {
        return "seen";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1)
            throw new WrongUsageException("/seen <username>");
        ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
        ServerGuardPermissionHandler.Player player = handler.getPlayer(args[0]);
        if (player == null)
            throw new PlayerNotFoundException();
        sender.addChatMessage(new ChatComponentText(player.username + " was last seen on " + player.lastSeen + "."));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
