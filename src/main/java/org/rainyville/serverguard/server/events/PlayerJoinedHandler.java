package org.rainyville.serverguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.util.Date;

public class PlayerJoinedHandler {
    @SubscribeEvent
    public void onJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (MinecraftServer.getServer() != null)
            MinecraftServer.getServer().getConfigurationManager().removeOp(event.player.getGameProfile());
        if (PermissionAPI.getPermissionHandler() instanceof ServerGuardPermissionHandler) {
            ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
            if (handler.getPlayer(event.player) == null) {
                ChunkCoordinates coords = event.player.worldObj.getSpawnPoint();
                event.player.setPositionAndUpdate(coords.posX, coords.posY, coords.posZ);
            }
            handler.registerPlayer(event.player);
            ServerGuardPermissionHandler.Player permissionPlayer = handler.getPlayer(event.player);
            if (permissionPlayer != null)
                permissionPlayer.lastSeen = new Date();
        }
    }
}
