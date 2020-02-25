package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

public class CommandSpawn extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Allows a player to teleport to spawn.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.spawn";
    }

    @Override
    public String getCommandName() {
        return "spawn";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ChunkCoordinates coords = sender.getEntityWorld().getSpawnPoint();
        if (args.length == 0 && sender instanceof EntityPlayer) {
            getCommandSenderAsPlayer(sender).setPositionAndUpdate(coords.posX, coords.posY, coords.posZ);
            notifyOperators(sender, this, 1, "Warped to spawn.");
        } else if (args.length == 1) {
            EntityPlayer player = getPlayer(sender, args[0]);
            player.setPositionAndUpdate(coords.posX, coords.posY, coords.posZ);
            notifyOperators(sender, this, 0, "%s warped to spawn.", sender.getCommandSenderName());
        }
    }
}
