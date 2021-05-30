package org.rainyville.protectguard.server.events;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.rainyville.protectguard.ProtectGuard;
import org.rainyville.protectguard.command.CommandInspect;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PlayerInteractionEvents {
    //TODO: [BUG] event gets called twice.
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        MinecraftServer server = event.getWorld().getMinecraftServer();
        BlockPos pos = event.getPos();
        if (server == null || !event.getSide().isServer()) return;

        try {
            if (CommandInspect.enabledInspector.contains(event.getEntityPlayer().getUniqueID())) {
                event.setCanceled(true);

                Statement stat = ProtectGuard.connection.createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM protectguard_place WHERE " +
                        "xPos BETWEEN " + (pos.getX() - 5) + " AND " + (pos.getX() + 5) +
                        " AND yPos BETWEEN " + (pos.getY() - 5) + " AND " + (pos.getY() + 5) +
                        " AND zPos BETWEEN " + (pos.getZ() - 5) + " AND " + (pos.getZ() + 5) + ";");
                if (!rs.next()) {
                    event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.GREEN + "No data available."));
                } else {
                    do {
                        String username = server.getPlayerProfileCache().getProfileByUUID(UUID.fromString(rs.getString("uuid"))).getName();
                        LocalDateTime timestamp = rs.getTimestamp("time").toLocalDateTime();
                        String time = timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                        String blockId = rs.getString("blockId");
                        int xPos = rs.getInt("xPos");
                        int yPos = rs.getInt("yPos");
                        int zPos = rs.getInt("zPos");
                        String message = String.format(TextFormatting.GOLD + "Player " + TextFormatting.YELLOW + "%s" + TextFormatting.GOLD + " placed block with ID " + TextFormatting.ITALIC + TextFormatting.GREEN + "%s " +
                                TextFormatting.GOLD + "at " + TextFormatting.BLUE + "%s " + TextFormatting.GOLD + "(x: %d, y: %d, z: %d).", username, blockId, time, xPos, yPos, zPos);
                        event.getEntityPlayer().sendMessage(new TextComponentString(message));
                    } while (rs.next());
                }
                rs.close();
            }
        } catch (Exception ex) {
            ProtectGuard.logger.error("Error occurred while executing a database query", ex);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        MinecraftServer server = event.getWorld().getMinecraftServer();
        BlockPos pos = event.getPos();
        if (server == null || !event.getSide().isServer()) return;

        try {
            if (CommandInspect.enabledInspector.contains(event.getEntityPlayer().getUniqueID())) {
                event.setCanceled(true);

                Statement stat = ProtectGuard.connection.createStatement();
                ResultSet rs = stat.executeQuery("SELECT * FROM protectguard_break WHERE " +
                        "xPos BETWEEN " + (pos.getX() - 5) + " AND " + (pos.getX() + 5) +
                        " AND yPos BETWEEN " + (pos.getY() - 5) + " AND " + (pos.getY() + 5) +
                        " AND zPos BETWEEN " + (pos.getZ() - 5) + " AND " + (pos.getZ() + 5) + ";");
                if (!rs.next()) {
                    event.getEntityPlayer().sendMessage(new TextComponentString(TextFormatting.GREEN + "No data available."));
                } else {
                    do {
                        String username = server.getPlayerProfileCache().getProfileByUUID(UUID.fromString(rs.getString("uuid"))).getName();
                        LocalDateTime timestamp = rs.getTimestamp("time").toLocalDateTime();
                        String time = timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                        String blockId = rs.getString("blockId");
                        int xPos = rs.getInt("xPos");
                        int yPos = rs.getInt("yPos");
                        int zPos = rs.getInt("zPos");
                        String message = String.format(TextFormatting.GOLD + "Player " + TextFormatting.YELLOW + "%s" + TextFormatting.GOLD + " destroyed block with ID " + TextFormatting.ITALIC + TextFormatting.RED + "%s " +
                                TextFormatting.GOLD + "at " + TextFormatting.BLUE + "%s " + TextFormatting.GOLD + "(x: %d, y: %d, z: %d).", username, blockId, time, xPos, yPos, zPos);
                        event.getEntityPlayer().sendMessage(new TextComponentString(message));
                    } while (rs.next());
                }
                rs.close();
            }
        } catch (Exception ex) {
            ProtectGuard.logger.error("Error occurred while executing a database query", ex);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        try {
            if (CommandInspect.enabledInspector.contains(event.getPlayer().getUniqueID())) {
                event.setCanceled(true);
                return;
            }

            BlockPos pos = event.getPos();
            Statement stat = ProtectGuard.connection.createStatement();

            stat.executeUpdate("create table if not exists protectguard_break (" +
                    "xPos INT," +
                    "yPos INT," +
                    "zPos INT," +
                    "uuid VARCHAR," +
                    "blockId VARCHAR," +
                    "time TIMESTAMP" +
                    ");");
            PreparedStatement prep = ProtectGuard.connection.prepareStatement(
                    "insert into protectguard_break values (?, ?, ?, ?, ?, ?);");

            prep.setInt(1, pos.getX());
            prep.setInt(2, pos.getY());
            prep.setInt(3, pos.getZ());
            prep.setString(4, event.getPlayer().getUniqueID().toString());
            prep.setString(5, Block.REGISTRY.getNameForObject(event.getState().getBlock()).toString());
            prep.setTimestamp(6, Timestamp.from(Instant.now()));
            prep.addBatch();

            ProtectGuard.connection.setAutoCommit(false);
            prep.executeBatch();
            ProtectGuard.connection.setAutoCommit(true);

            clearHistory();
        } catch (Exception ex) {
            ProtectGuard.logger.error("Exception occurred while executing a database query", ex);
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        try {
            if (CommandInspect.enabledInspector.contains(event.getPlayer().getUniqueID())) {
                event.setCanceled(true);
                return;
            }

            BlockPos pos = event.getPos();
            Statement stat = ProtectGuard.connection.createStatement();

            stat.executeUpdate("create table if not exists protectguard_place (" +
                    "xPos INT," +
                    "yPos INT," +
                    "zPos INT," +
                    "uuid VARCHAR," +
                    "blockId VARCHAR," +
                    "time TIMESTAMP" +
                    ");");
            PreparedStatement prep = ProtectGuard.connection.prepareStatement(
                    "insert into protectguard_place values (?, ?, ?, ?, ?, ?);");

            prep.setInt(1, pos.getX());
            prep.setInt(2, pos.getY());
            prep.setInt(3, pos.getZ());
            prep.setString(4, event.getPlayer().getUniqueID().toString());
            prep.setString(5, Block.REGISTRY.getNameForObject(event.getPlacedBlock().getBlock()).toString());
            prep.setTimestamp(6, Timestamp.from(Instant.now()));
            prep.addBatch();

            ProtectGuard.connection.setAutoCommit(false);
            prep.executeBatch();
            ProtectGuard.connection.setAutoCommit(true);

            clearHistory();
        } catch (Exception ex) {
            ProtectGuard.logger.error("Exception occurred while executing a database query", ex);
        }
    }

    //TODO: Implement passive clearing of old database data older than 48 hours.
    private void clearHistory() {}
}
