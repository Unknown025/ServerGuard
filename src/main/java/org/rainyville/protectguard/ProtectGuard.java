package org.rainyville.protectguard;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rainyville.protectguard.command.CommandInspect;
import org.rainyville.protectguard.proxy.CommonProxy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Main class for the ProtectGuard mod.
 */
@SuppressWarnings("unused")
@Mod(modid = ProtectGuard.MODID, version = ProtectGuard.VERSION, name = ProtectGuard.NAME, acceptableRemoteVersions = "*")
public class ProtectGuard {
    public static final String MODID = "protectguard";
    public static final String VERSION = "1.0";
    public static final String NAME = "ProtectGuard";
    public static final Logger logger = LogManager.getLogger("ProtectGuard");

    public static Connection connection;

    @SidedProxy(clientSide = "org.rainyville.protectguard.proxy.ClientProxy", serverSide = "org.rainyville.protectguard.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide() != Side.SERVER) return;
        proxy.registerEvents();
        proxy.init(event);
        logger.info("Registered proxies.");

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:protectguard.db");
        } catch (SQLException | ClassNotFoundException ex) {
            logger.error("SQL error", ex);
        }
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandInspect());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}
