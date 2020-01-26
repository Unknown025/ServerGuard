package org.rainyville.serverguard.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;

public abstract class PermissionCommandBase extends CommandBase {
    public PermissionCommandBase() {
        PermissionAPI.getPermissionHandler().registerNode(getPermissionNode(), DefaultPermissionLevel.NONE, "Default permission description.");
    }

    /**
     * Permission node for this command.
     *
     * @return Returns permission node.
     */
    public abstract String getPermissionNode();

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (sender instanceof EntityPlayer)
            return PermissionAPI.getPermissionHandler().hasPermission(getCommandSenderAsPlayer(sender).getGameProfile(), getPermissionNode(), null);
        return super.canCommandSenderUseCommand(sender);
    }
}
