package org.rainyville.serverguard;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.command.ICommand;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.serverguard.command.*;
import org.rainyville.serverguard.proxy.CommonProxy;
import org.rainyville.serverguard.server.DiscordBridge;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.io.File;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = ServerGuard.MODID, version = ServerGuard.VERSION, acceptableRemoteVersions = "*")
public class ServerGuard {
    public static final String MODID = "serverguard";
    public static final String VERSION = "1.0";
    public static final Logger logger = LogManager.getLogger("ServerGuard");

    @SidedProxy(serverSide = "org.rainyville.serverguard.proxy.CommonProxy", clientSide = "org.rainyville.serverguard.proxy.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandGameMode());
        event.registerServerCommand(new CommandKill());
        event.registerServerCommand(new CommandAdmin());
        event.registerServerCommand(new CommandWhoIs());
        event.registerServerCommand(new CommandPex());
        event.registerServerCommand(new CommandSpawn());
    }

    @SuppressWarnings("unchecked")
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        Map<String, ICommand> commandMap = MinecraftServer.getServer().getCommandManager().getCommands();
        for (Map.Entry<String, ICommand> set : commandMap.entrySet()) {
            if (set.getValue() instanceof PermissionCommandBase)
                continue;
            set.setValue(PermissionCommandBase.fromICommand(set.getValue()));
            ServerGuard.logger.info("Registered command " + set.getValue().getCommandName() + " with permission node " + ((PermissionCommandBase) set.getValue()).getPermissionNode());
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() != Side.SERVER) return;
        proxy.registerEvents();
        proxy.init(event);
        logger.info("Registered proxies.");
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PermissionAPI.setPermissionHandler(
                new ServerGuardPermissionHandler(new File(event.getModConfigurationDirectory(), "permissions.json")));
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        String token = configuration.getString
                ("token", "Discord", null, "The token for Discord bot functionality.");
        String reportChannel = configuration.getString
                ("report_channel", "Discord", null, "Channel ID for the report channel.");
        DiscordBridge.initialize(token, reportChannel);
    }
}
