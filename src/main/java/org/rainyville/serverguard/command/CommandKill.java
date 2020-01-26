package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;

public class CommandKill extends PermissionCommandBase {
    @Override
    public String getCommandName() {
        return "kill";
    }

    @Override
    public String getPermissionNode() {
        return "minecraft.command.kill";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.kill.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP entityplayermp = null;
        if (args.length == 0) {
            entityplayermp = getCommandSenderAsPlayer(sender);
        } else if (args.length == 1) {
            entityplayermp = getPlayer(sender, args[0]);
        }
        if (entityplayermp == null) {
            throw new PlayerNotFoundException();
        }
        entityplayermp.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
        if (entityplayermp == sender) {
            sender.addChatMessage(new ChatComponentTranslation("commands.kill.success"));
        } else {
            notifyOperators(sender, this, "commands.kill.success.other", entityplayermp.getCommandSenderName());
        }
    }
}
