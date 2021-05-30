package org.rainyville.serverguard;

import net.minecraft.command.ICommand;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.serverguard.command.CommandReport;
import org.rainyville.serverguard.proxy.CommonProxy;
import org.rainyville.serverguard.server.DiscordBridge;

import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = ServerGuard.MODID, version = ServerGuard.VERSION, acceptableRemoteVersions = "*", certificateFingerprint = "@FINGERPRINT@")
public class ServerGuard {
    public static final String MODID = "serverguard";
    public static final String VERSION = "1.0";
    public static final Logger logger = LogManager.getLogger("ServerGuard");

    @SidedProxy(serverSide = "org.rainyville.serverguard.proxy.CommonProxy", clientSide = "org.rainyville.serverguard.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            event.registerServerCommand(new CommandReport());
            Map<String, ICommand> commandMap = event.getServer().getCommandManager().getCommands();
            //Add unban alias.
            if (commandMap.containsKey("pardon"))
                commandMap.put("unban", commandMap.get("pardon"));
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        logger.info("Registered proxies.");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        String token = configuration.getString
                ("token", "Discord", "", "The token for Discord bot functionality.");
        String reportChannel = configuration.getString
                ("report_channel", "Discord", "", "Channel ID for the report channel.");
        configuration.save();
        DiscordBridge.initialize(token, reportChannel);
    }
}
