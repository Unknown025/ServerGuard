package org.rainyville.protectguard.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

public class CommandInspect extends CommandBase {
    public static List<UUID> enabledInspector = new ArrayList<>();

    @Override
    public String getName() {
        return "inspect";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/inspect";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayer))
            throw new CommandException("commands.generic.exception");
        if (args.length != 0) {
            throw new SyntaxErrorException();
        }
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        UUID id = player.getUniqueID();
        if (enabledInspector.remove(id)) {
            player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Inspector disabled"));
        } else {
            enabledInspector.add(id);
            player.sendMessage(new TextComponentString(TextFormatting.GOLD + "Inspector enabled"));
        }
    }
}
