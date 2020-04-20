package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.List;

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
        return "/whois [player]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1)
            throw new WrongUsageException("/whois [player]");
        EntityPlayerMP player = getPlayer(sender, args[0]);
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "---- " +
                EnumChatFormatting.RED + "Player: " + player.getCommandSenderName() +
                EnumChatFormatting.GOLD + " ----"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "UUID: " +
                EnumChatFormatting.RED + player.getUniqueID()));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Health: " +
                EnumChatFormatting.RED + player.getHealth() + "/" + player.getMaxHealth()));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Hunger: " +
                EnumChatFormatting.RED + player.getFoodStats().getFoodLevel() + "/20"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Alive: " +
                EnumChatFormatting.RED + player.isEntityAlive()));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Gamemode: " +
                EnumChatFormatting.RED + (player.capabilities.isCreativeMode ? "1" : (player.capabilities.allowEdit ? "0" : "2"))));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Location: " +
                EnumChatFormatting.RED + "(" + (int) player.posX + ", " + (int) player.posY + ", " + (int) player.posZ + ")"));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Dimension: " +
                EnumChatFormatting.RED + player.dimension));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "IP Address: " +
                EnumChatFormatting.GREEN + player.getPlayerIP()));
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Opped: " +
                EnumChatFormatting.GREEN + player.mcServer.getConfigurationManager().canSendCommands(player.getGameProfile())));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
