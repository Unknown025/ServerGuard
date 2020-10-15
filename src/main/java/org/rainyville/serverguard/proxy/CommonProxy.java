package org.rainyville.serverguard.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import org.rainyville.serverguard.command.CommandInventorySee;
import org.rainyville.serverguard.server.events.PlayerJoinedHandler;
import org.rainyville.serverguard.server.events.ServerTickHandler;

public class CommonProxy {
    @SuppressWarnings("unused")
    public void init(FMLInitializationEvent event) {
        FMLCommonHandler.instance().bus().register(new PlayerJoinedHandler());
        FMLCommonHandler.instance().bus().register(new CommandInventorySee());
        FMLCommonHandler.instance().bus().register(new ServerTickHandler());
    }
}
