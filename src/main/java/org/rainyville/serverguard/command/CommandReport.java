package org.rainyville.serverguard.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.rainyville.serverguard.server.DiscordBridge;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

public class CommandReport extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.ALL;
    }

    @Override
    public String getDescription() {
        return "Allows players to report each other.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.report";
    }

    @Override
    public String getCommandName() {
        return "report";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new SyntaxErrorException("/report <player> <reason>");
        }
        EntityPlayerMP report = getPlayer(sender, args[0]);
        EntityPlayerMP originator = getCommandSenderAsPlayer(sender);

        if (report.getGameProfile().equals(originator.getGameProfile())) {
            throw new CommandException("Cannot report yourself!");
        }

        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]);
            reason.append(" ");
        }
        DiscordBridge.reportPlayer(report, originator, reason.toString().trim());
        originator.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.DARK_GREEN + "Reported " +
                EnumChatFormatting.RED + report.getCommandSenderName() + EnumChatFormatting.DARK_GREEN + "!"));
    }
}
