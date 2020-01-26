package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

public class CommandWhoIs extends PermissionCommandBase {
    @Override
    public String getCommandName() {
        return "whois";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Retrieves all available information for a player.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.whois";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.whois.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        
    }
}
