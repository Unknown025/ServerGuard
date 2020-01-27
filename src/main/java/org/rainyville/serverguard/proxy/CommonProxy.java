package org.rainyville.serverguard.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.rainyville.serverguard.server.events.PlayerJoinedHandler;

public class CommonProxy {
    public void registerEvents() {
//        MinecraftForge.EVENT_BUS.register(new CommandHandler());
    }

    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new PlayerJoinedHandler());
    }

    public void preInit(FMLPreInitializationEvent event) {}
}
