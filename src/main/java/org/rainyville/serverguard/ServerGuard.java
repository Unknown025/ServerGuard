package org.rainyville.serverguard;

import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.serverguard.command.CommandReport;
import org.rainyville.serverguard.proxy.CommonProxy;
import org.rainyville.serverguard.server.DiscordBridge;

import java.awt.*;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
@Mod(modid = ServerGuard.MODID, version = ServerGuard.VERSION, acceptableRemoteVersions = "*", certificateFingerprint = "@FINGERPRINT@")
public class ServerGuard {
    public static final String MODID = "serverguard";
    public static final String VERSION = "1.0";
    public static final Logger LOGGER = LogManager.getLogger("ServerGuard");

    public static final String SERVER_FINE = "<:server:1319879957102465074>";
    public static final String SERVER_WARN = "<:server_warn:1319879914241134693>";
    public static final String SERVER_ERROR = "<:server_offline:1319879844624072745>";

    @SidedProxy(serverSide = "org.rainyville.serverguard.proxy.CommonProxy", clientSide = "org.rainyville.serverguard.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            event.registerServerCommand(new CommandReport());
            Map<String, ICommand> commandMap = event.getServer().getCommandManager().getCommands();
            //Add unban alias.
            if (commandMap.containsKey("pardon"))
                commandMap.put("unban", commandMap.get("pardon"));
        }
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setDescription(SERVER_FINE + " Server is online.");
        DiscordBridge.logUptime(builder.build());

        Runtime.getRuntime().addShutdownHook(new Thread(ServerGuard::serverOffline));
    }

    private static void serverOffline() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED);
        builder.setDescription(SERVER_ERROR + " Server is offline.");
        DiscordBridge.logUptime(builder.build());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        LOGGER.info("Registered proxies.");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        String token = configuration.getString
                ("token", "Discord", "", "The token for Discord bot functionality.");
        String reportChannel = configuration.getString
                ("report_channel", "Discord", "", "Channel ID for the report channel.");
        String uptimeChannel = configuration.getString
                ("uptime_channel", "Discord", "", "Channel ID for the uptime channel.");
        configuration.save();
        DiscordBridge.initialize(token, reportChannel, uptimeChannel);
    }
}
