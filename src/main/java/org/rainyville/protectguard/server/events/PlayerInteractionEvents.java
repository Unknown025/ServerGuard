package org.rainyville.protectguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import org.rainyville.protectguard.ProtectGuard;
import org.rainyville.protectguard.command.CommandInspect;

import java.sql.*;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PlayerInteractionEvents {
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:protectguard.db");
            Statement stat = conn.createStatement();

            if (CommandInspect.enabledInspector.contains(event.getPlayer().getUniqueID())) {
                event.setCanceled(true);
                ResultSet rs = stat.executeQuery("select * from protectguard_break where xPos=" + event.x + " and yPos=" + event.y + " and zPos=" + event.z + ";");
                if (!rs.next()) {
                    event.getPlayer().addChatComponentMessage(new ChatComponentText("No data available."));
                } else {
                    do {
                        String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                        String time = rs.getTimestamp("time").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        event.getPlayer().addChatComponentMessage(new ChatComponentTranslation("commands.inspect.destroyed", EnumChatFormatting.YELLOW + username, rs.getString("blockId"), time));
                    } while (rs.next());
                }
                rs.close();
                return;
            }
            stat.executeUpdate("create table if not exists protectguard_break (" +
                    "xPos INT," +
                    "yPos INT," +
                    "zPos INT," +
                    "uuid VARCHAR," +
                    "blockId VARCHAR," +
                    "time TIMESTAMP" +
                    ");");
            PreparedStatement prep = conn.prepareStatement(
                    "insert into protectguard_break values (?, ?, ?, ?, ?, ?);");

            prep.setInt(1, event.x);
            prep.setInt(2, event.y);
            prep.setInt(3, event.z);
            prep.setString(4, event.getPlayer().getUniqueID().toString());
            prep.setString(5, Block.blockRegistry.getNameForObject(event.block));
            prep.setTimestamp(6, Timestamp.from(Instant.now()));
            prep.addBatch();

            conn.setAutoCommit(false);
            prep.executeBatch();
            conn.setAutoCommit(true);
            conn.close();
        } catch (Exception ex) {
            ProtectGuard.logger.error("Exception occurred while executing a database query", ex);
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:protectguard.db");
            Statement stat = conn.createStatement();

            if (CommandInspect.enabledInspector.contains(event.player.getUniqueID())) {
                event.setCanceled(true);
                ResultSet rs = stat.executeQuery("select * from protectguard_place where xPos=" + event.x + " and yPos=" + event.y + " and zPos=" + event.z + ";");
                if (!rs.next()) {
                    event.player.addChatComponentMessage(new ChatComponentText("No data available."));
                } else {
                    do {
                        String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                        String time = rs.getTimestamp("time").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//                        event.player.addChatComponentMessage(new ChatComponentText(String.format("Player %s placed block with ID \"%s\" at %s", username, rs.getString("blockId"), time)));
                        event.player.addChatComponentMessage(new ChatComponentTranslation("commands.inspect.placed", EnumChatFormatting.YELLOW + username, rs.getString("blockId"), time));
                    } while (rs.next());
                }
                rs.close();
                return;
            }

            stat.executeUpdate("create table if not exists protectguard_place (" +
                    "xPos INT," +
                    "yPos INT," +
                    "zPos INT," +
                    "uuid VARCHAR," +
                    "blockId VARCHAR," +
                    "time TIMESTAMP" +
                    ");");
            PreparedStatement prep = conn.prepareStatement(
                    "insert into protectguard_place values (?, ?, ?, ?, ?, ?);");

            prep.setInt(1, event.x);
            prep.setInt(2, event.y);
            prep.setInt(3, event.z);
            prep.setString(4, event.player.getUniqueID().toString());
            prep.setString(5, Block.blockRegistry.getNameForObject(event.block));
            prep.setTimestamp(6, Timestamp.from(Instant.now()));
            prep.addBatch();

            conn.setAutoCommit(false);
            prep.executeBatch();
            conn.setAutoCommit(true);
            conn.close();
        } catch (Exception ex) {
            ProtectGuard.logger.error("Exception occurred while executing a database query", ex);
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:protectguard.db");
            Statement stat = conn.createStatement();

            if (CommandInspect.enabledInspector.contains(event.entityPlayer.getUniqueID())) {
                event.setCanceled(true);
                if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    ResultSet rs = stat.executeQuery("select * from protectguard_place where xPos=" + event.x + " and yPos=" + event.y + " and zPos=" + event.z + ";");
                    if (!rs.next()) {
                        event.entityPlayer.addChatComponentMessage(new ChatComponentText("No data available."));
                    } else {
                        do {
                            String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                            String time = rs.getTimestamp("time").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("commands.inspect.placed", EnumChatFormatting.YELLOW + username, rs.getString("blockId"), time));
                        } while (rs.next());
                    }
                    rs.close();
                } else if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK){
                    ResultSet rs = stat.executeQuery("select * from protectguard_break where xPos=" + event.x + " and yPos=" + event.y + " and zPos=" + event.z + ";");
                    if (!rs.next()) {
                        event.entityPlayer.addChatComponentMessage(new ChatComponentText("No data available."));
                    } else {
                        do {
                            String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                            String time = rs.getTimestamp("time").toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
//                            event.entityPlayer.addChatComponentMessage(new ChatComponentText(String.format("Player %s destroyed block with ID \"%s\" at %s", username, rs.getString("blockId"), time)));
                            event.entityPlayer.addChatComponentMessage(new ChatComponentTranslation("commands.inspect.destroyed", EnumChatFormatting.YELLOW + username, rs.getString("blockId"), time));
                        } while (rs.next());
                    }
                    rs.close();
                }
                return;
            }
            conn.close();
        } catch (Exception ex) {
            ProtectGuard.logger.error("Exception occurred while executing a database query", ex);
        }
    }
}
