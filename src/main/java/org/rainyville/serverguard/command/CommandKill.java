package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

public class CommandKill extends PermissionCommandBase {
    @Override
    public String getCommandName() {
        return "kill";
    }

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "";
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
        EntityPlayerMP entityPlayerMP = null;
        if (args.length == 0) {
            entityPlayerMP = getCommandSenderAsPlayer(sender);
        } else if (args.length == 1) {
            entityPlayerMP = getPlayer(sender, args[0]);
        }
        if (entityPlayerMP == null) {
            throw new PlayerNotFoundException();
        }
        entityPlayerMP.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
        if (entityPlayerMP == sender) {
            sender.addChatMessage(new ChatComponentTranslation("commands.kill.success"));
        } else {
            notifyOperators(sender, this, "commands.kill.success.other", entityPlayerMP.getCommandSenderName());
        }
    }
}
