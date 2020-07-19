package org.rainyville.protectguard.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.*;

public class CommandInspect extends CommandBase {
    public static List<UUID> enabledInspector = new ArrayList<>();

    @Override
    public String getCommandName() {
        return "inspect";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/inspect";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer))
            throw new CommandException("commands.generic.exception");
        if (args.length != 0) {
            throw new SyntaxErrorException();
        }
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        UUID id = player.getUniqueID();
        if (enabledInspector.remove(id)) {
            player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Inspector disabled."));
        } else {
            enabledInspector.add(id);
            player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Inspector enabled."));
        }
    }
}
