package org.rainyville.serverguard;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.serverguard.command.CommandAdmin;
import org.rainyville.serverguard.command.CommandGameMode;
import org.rainyville.serverguard.command.CommandKill;

@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = ServerGuard.MODID, version = ServerGuard.VERSION, acceptableRemoteVersions = "*")
public class ServerGuard {
    public static final String MODID = "serverguard";
    public static final String VERSION = "1.0";
    public static final Logger logger = LogManager.getLogger("ServerGuard");

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandGameMode());
        event.registerServerCommand(new CommandKill());
        event.registerServerCommand(new CommandAdmin());
    }
}
