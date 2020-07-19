package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.List;

public class CommandHeal extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Heals a player back to full health.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.heal";
    }

    @Override
    public String getCommandName() {
        return "heal";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerMP && args.length == 0) {
            EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
            playerMP.setHealth(playerMP.getMaxHealth());
            playerMP.getFoodStats().addStats(20, 0);
            playerMP.addChatMessage(new ChatComponentText("Healed to full health"));
            notifyOperators(sender, this, 1, "Healed themselves");
        } else if (args.length == 1) {
            EntityPlayerMP playerMP = getPlayer(sender, args[0]);
            playerMP.setHealth(playerMP.getMaxHealth());
            playerMP.getFoodStats().addStats(20, 0);
            notifyOperators(sender, this, "Healed %s", playerMP.getCommandSenderName());
        } else {
            throw new WrongUsageException("/heal [username]");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
