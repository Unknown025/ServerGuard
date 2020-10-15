package org.rainyville.serverguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import org.rainyville.serverguard.ServerGuard;
import org.rainyville.serverguard.command.CommandFly;

import java.time.OffsetDateTime;

public class ServerTickHandler {
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        if (CommandFly.lastCacheReset.isBefore(OffsetDateTime.now().minusDays(1))) {
            CommandFly.usageMap.clear();
            CommandFly.lastCacheReset = OffsetDateTime.now();
            ServerGuard.logger.info("Reset fly command usage cache");
        }
    }
}
