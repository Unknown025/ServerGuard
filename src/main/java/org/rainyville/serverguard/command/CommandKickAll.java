package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.List;

public class CommandKickAll extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Kicks all members from the server.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.kickall";
    }

    @Override
    public String getCommandName() {
        return "kickall";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String reason = "Kicked by an operator.";
        boolean flag = false;
        if (args.length > 0) {
            if (args.length >= 2) {
                reason = getChatComponentFromNthArg(sender, args, 0).getUnformattedText();
                flag = true;
            }
        }
        for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            ((EntityPlayerMP)player).playerNetServerHandler.kickPlayerFromServer(reason);
        }
        if (flag) {
            notifyOperators(sender, this, "Kicked all players from the server for \"" + reason + "\"");
        } else {
            notifyOperators(sender, this, "Kicked all players from the server.");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
