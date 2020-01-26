package org.rainyville.protectguard.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.rainyville.protectguard.server.events.PlayerInteractionEvents;

public class CommonProxy {
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new PlayerInteractionEvents());
    }

    public void init(FMLInitializationEvent event) {}

    public void preInit(FMLPreInitializationEvent event) {}
}
