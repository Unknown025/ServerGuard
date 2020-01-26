package org.rainyville.serverguard.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class WhoisCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "whois";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.whois.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        
    }
}
