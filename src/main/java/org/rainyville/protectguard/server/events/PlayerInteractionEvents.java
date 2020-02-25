package org.rainyville.protectguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.rainyville.protectguard.ProtectGuard;
import org.rainyville.protectguard.command.CommandInspect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PlayerInteractionEvents {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:protectguard.db");
            Statement stat = conn.createStatement();

            if (CommandInspect.enabledInspector.contains(event.entityPlayer.getUniqueID())) {
                event.setCanceled(true);
                if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                    ResultSet rs = stat.executeQuery("SELECT * FROM protectguard_place WHERE " +
                            "xPos BETWEEN " + (event.x - 5) + " AND " + (event.x + 5) +
                            " AND yPos BETWEEN " + (event.y - 5) + " AND " + (event.y + 5) +
                            " AND zPos BETWEEN " + (event.z - 5) + " AND " + (event.z + 5) + ";");
                    if (!rs.next()) {
                        event.entityPlayer.addChatComponentMessage(new ChatComponentText("No data available."));
                    } else {
                        do {
                            String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                            LocalDateTime timestamp = rs.getTimestamp("time").toLocalDateTime();
                            String time = timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                            String blockId = rs.getString("blockId");
                            int xPos = rs.getInt("xPos");
                            int yPos = rs.getInt("yPos");
                            int zPos = rs.getInt("zPos");
                            String message = String.format(EnumChatFormatting.GOLD + "Player " + EnumChatFormatting.YELLOW + "%s" + EnumChatFormatting.GOLD + " placed block with ID " + EnumChatFormatting.ITALIC + EnumChatFormatting.GREEN + "%s " +
                                    EnumChatFormatting.GOLD + "at " + EnumChatFormatting.BLUE + "%s " + EnumChatFormatting.GOLD + "(x: %d, y: %d, z: %d).", username, blockId, time, xPos, yPos, zPos);
                            event.entityPlayer.addChatComponentMessage(new ChatComponentText(message));
                        } while (rs.next());
                    }
                    rs.close();
                } else if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                    ResultSet rs = stat.executeQuery("SELECT * FROM protectguard_break WHERE " +
                            "xPos BETWEEN " + (event.x - 5) + " AND " + (event.x + 5) +
                            " AND yPos BETWEEN " + (event.y - 5) + " AND " + (event.y + 5) +
                            " AND zPos BETWEEN " + (event.z - 5) + " AND " + (event.z + 5) + ";");
                    if (!rs.next()) {
                        event.entityPlayer.addChatComponentMessage(new ChatComponentText("No data available."));
                    } else {
                        do {
                            String username = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(UUID.fromString(rs.getString("uuid"))).getName();
                            LocalDateTime timestamp = rs.getTimestamp("time").toLocalDateTime();
                            String time = timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                            String blockId = rs.getString("blockId");
                            int xPos = rs.getInt("xPos");
                            int yPos = rs.getInt("yPos");
                            int zPos = rs.getInt("zPos");
                            String message = String.format(EnumChatFormatting.GOLD + "Player " + EnumChatFormatting.YELLOW + "%s" + EnumChatFormatting.GOLD + " destroyed block with ID " + EnumChatFormatting.ITALIC + EnumChatFormatting.RED + "%s " +
                                    EnumChatFormatting.GOLD + "at " + EnumChatFormatting.BLUE + "%s " + EnumChatFormatting.GOLD + "(x: %d, y: %d, z: %d).", username, blockId, time, xPos, yPos, zPos);
                            event.entityPlayer.addChatComponentMessage(new ChatComponentText(message));
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
