package org.rainyville.serverguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.util.Date;
import java.util.Objects;

public class PlayerJoinedHandler {
    @SubscribeEvent
    public void onJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        if (MinecraftServer.getServer() != null) {
            MinecraftServer.getServer().getConfigurationManager().removeOp(event.player.getGameProfile());
        }
        if (PermissionAPI.getPermissionHandler() instanceof ServerGuardPermissionHandler) {
            ((ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler()).registerPlayer(event.player);
            Objects.requireNonNull(((ServerGuardPermissionHandler)
                    PermissionAPI.getPermissionHandler()).getPlayer(event.player)).lastSeen = new Date();
        }
    }
}
