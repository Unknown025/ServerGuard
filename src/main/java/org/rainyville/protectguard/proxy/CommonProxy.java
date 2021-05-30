package org.rainyville.protectguard.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.rainyville.protectguard.server.events.PlayerInteractionEvents;

public class CommonProxy {
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new PlayerInteractionEvents());
    }

    public void init(FMLInitializationEvent event) {}

    public void preInit(FMLPreInitializationEvent event) {}
}
