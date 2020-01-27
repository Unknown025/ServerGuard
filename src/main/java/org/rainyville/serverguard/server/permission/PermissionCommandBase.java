package org.rainyville.serverguard.server.permission;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public abstract class PermissionCommandBase extends CommandBase {
    public PermissionCommandBase() {
        PermissionAPI.getPermissionHandler().registerNode(getPermissionNode(), getPermissionLevel(), getDescription());
    }

    /**
     * Gets the default permission level.
     *
     * @return permission level for this command.
     */
    public abstract DefaultPermissionLevel getPermissionLevel();

    /**
     * Gets the description for this permission node.
     *
     * @return Permission node description.
     */
    public abstract String getDescription();

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
        if (sender instanceof EntityPlayer) {
            boolean canUseCommand = PermissionAPI.getPermissionHandler().hasPermission(getCommandSenderAsPlayer(sender).getGameProfile(), getPermissionNode(), null);
            if (!canUseCommand) {
                MinecraftServer.getServer().logInfo(sender.getCommandSenderName() + " issued server command: /" + getCommandName());
                MinecraftServer.getServer().logWarning(sender.getCommandSenderName() + " was denied access to command.");
            }
            return canUseCommand;
        }
        return super.canCommandSenderUseCommand(sender);
    }

    /**
     * Creates a PermissionCommandBase object from an ICommand.
     *
     * @param command ICommand to transform.
     * @return Returns a Permission interface compatible instance.
     */
    public static PermissionCommandBase fromICommand(ICommand command) {
        return new PermissionCommandBase() {
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
                return command.getClass().getCanonicalName();
            }

            @Override
            public String getCommandName() {
                return command.getCommandName();
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                command.processCommand(sender, args);
            }
        };
    }
}
