package org.rainyville.serverguard.server;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.minecraft.entity.player.EntityPlayerMP;
import org.rainyville.serverguard.ServerGuard;

import javax.security.auth.login.LoginException;
import java.awt.*;

/**
 * PACKAGE: org.rainyville.serverguard.server
 * DATE: 3/3/20
 * TIME: 6:46 PM
 * PROJECT: ServerGuard
 */
public class DiscordBridge {
    private static JDA bot = null;
    private static TextChannel reportChannel = null;
    private static TextChannel uptimeChannel = null;

    /**
     * Initializes the DiscordBridge.
     *
     * @param token Token to authenticate with.
     */
    public static void initialize(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            bot = JDABuilder.createDefault(token).setStatus(OnlineStatus.ONLINE).build();
            ServerGuard.LOGGER.info("Discord bot invite link: {}", bot.getInviteUrl(Permission.ADMINISTRATOR));
        } catch (LoginException e) {
            ServerGuard.LOGGER.error("Exception when initializing DiscordBridge!", e);
        }
    }

    /**
     * Initializes the DiscordBridge.
     *
     * @param token           Token to authenticate with.
     * @param reportChannelId Channel ID to use for reports.
     */
    public static void initialize(String token, String reportChannelId, String uptimeChannelId) {
        initialize(token);
        if (bot == null) return;
        try {
            bot.awaitReady();
            reportChannel = bot.getTextChannelById(reportChannelId);
            uptimeChannel = bot.getTextChannelById(uptimeChannelId);
            if (reportChannel == null)
                ServerGuard.LOGGER.warn("Discord report channel null!");
            if (uptimeChannel == null)
                ServerGuard.LOGGER.warn("Discord uptime channel null!");
        } catch (Exception ex) {
            ServerGuard.LOGGER.error(ex);
        }
    }

    /**
     * Reports a player.
     *
     * @param reported   Player to report.
     * @param originator Player who wants to report another player.
     * @param reason     Reason for the report.
     */
    public static void reportPlayer(EntityPlayerMP reported, EntityPlayerMP originator, String reason) {
        if (reportChannel == null) return;
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(originator.getName());
        builder.setTitle(reported.getName() + " Reported");
        builder.setColor(Color.ORANGE);
        builder.addField("Reason", reason, false);
        reportChannel.sendMessageEmbeds(builder.build()).queue();
    }

    /**
     * Logs a message.
     *
     * @param message Message.
     */
    public static void logMessage(Message message) {
        if (reportChannel == null) return;
        reportChannel.sendMessage(message).queue();
    }

    /**
     * Logs a message.
     *
     * @param message MessageEmbed.
     */
    public static void logMessage(MessageEmbed message) {
        if (reportChannel == null) return;
        reportChannel.sendMessageEmbeds(message).queue();
    }

    /**
     * Logs a message for the status of the server (i.e. starting, stopping, etc.).
     *
     * @param message Message to send.
     */
    public static void logUptime(MessageEmbed message) {
        if (uptimeChannel == null) return;
        uptimeChannel.sendMessageEmbeds(message).complete();
    }

    /**
     * Retrieves the JDA instance, useful for registering events or other Discord interfaces.
     *
     * @return {@link JDA} instance.
     */
    public static JDA getJDA() {
        return bot;
    }
}
