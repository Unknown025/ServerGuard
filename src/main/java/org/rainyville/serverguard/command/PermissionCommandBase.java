package org.rainyville.serverguard.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import org.rainyville.serverguard.server.permission.PermissionAPI;

public abstract class PermissionCommandBase extends CommandBase {
    /**
     * Permission node for this command.
     * @return Returns permission node.
     */
    public abstract String getPermissionNode();

    @Override
    public String getCommandName() {
        return null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if(sender instanceof EntityPlayer)
            return PermissionAPI.getPermissionHandler().hasPermission(getCommandSenderAsPlayer(sender).getGameProfile(), getPermissionNode(), null);
        return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }
}
