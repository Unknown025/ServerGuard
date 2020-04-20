package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.List;

public class CommandFly extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Allows for enabling flight.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.fly";
    }

    @Override
    public String getCommandName() {
        return "fly";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fly [username]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 && sender instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
            playerMP.capabilities.allowFlying = !playerMP.capabilities.allowFlying;
            if (!playerMP.onGround)
                playerMP.capabilities.isFlying = playerMP.capabilities.allowFlying;
            playerMP.sendPlayerAbilities();
            notifyOperators(sender, this, playerMP.capabilities.allowFlying ?
                    "Enabled flying" : "Disabled flying");
        } else if (args.length == 1) {
            EntityPlayerMP playerMP = getPlayer(sender, args[0]);
            playerMP.capabilities.allowFlying = !playerMP.capabilities.allowFlying;
            if (!playerMP.onGround)
                playerMP.capabilities.isFlying = playerMP.capabilities.allowFlying;
            playerMP.sendPlayerAbilities();
            notifyOperators(sender, this, playerMP.capabilities.allowFlying ?
                    "Enabled flying for %s" : "Disabled flying for %s", playerMP.getCommandSenderName());
        } else {
            throw new WrongUsageException("/fly [username]");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
