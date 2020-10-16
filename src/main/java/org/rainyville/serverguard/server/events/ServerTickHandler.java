package org.rainyville.serverguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.rainyville.serverguard.ServerGuard;
import org.rainyville.serverguard.command.CommandFly;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.time.OffsetDateTime;
import java.util.UUID;

public class ServerTickHandler {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (CommandFly.lastCacheReset.isBefore(OffsetDateTime.now().minusDays(1))) {
//            ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
//            for (UUID uuid : CommandFly.usageMap.keySet()) {
//                ServerGuardPermissionHandler.Player player = handler.getPlayer(uuid);
//                if (player != null) player.permissions.remove("-serverguard.command.fly");
//            }
            CommandFly.usageMap.clear();
            CommandFly.lastCacheReset = OffsetDateTime.now();
            ServerGuard.logger.info("Reset fly command usage cache");
        }
    }
}
