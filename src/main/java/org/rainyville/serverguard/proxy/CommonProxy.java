package org.rainyville.serverguard.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.rainyville.serverguard.server.events.CommandHandler;

public class CommonProxy {
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.register(new CommandHandler());
    }

    public void init(FMLInitializationEvent event) {}

    public void preInit(FMLPreInitializationEvent event) {}
}
