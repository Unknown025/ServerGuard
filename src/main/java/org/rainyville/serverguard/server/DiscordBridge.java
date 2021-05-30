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

    /**
     * Initializes the DiscordBridge.
     *
     * @param token Token to authenticate with.
     */
    public static void initialize(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            bot = JDABuilder.createDefault(token).setStatus(OnlineStatus.ONLINE).build();
            ServerGuard.logger.info("Discord bot invite link: " + bot.getInviteUrl(Permission.ADMINISTRATOR));
        } catch (LoginException e) {
            ServerGuard.logger.error("Exception when initializing DiscordBridge!", e);
        }
    }

    /**
     * Initializes the DiscordBridge.
     *
     * @param token           Token to authenticate with.
     * @param reportChannelId Channel ID to use for reports.
     */
    public static void initialize(String token, String reportChannelId) {
        initialize(token);
        if (bot == null || reportChannelId == null || reportChannelId.isEmpty()) return;
        try {
            bot.awaitReady();
            reportChannel = bot.getTextChannelById(reportChannelId);
            if (reportChannel == null)
                ServerGuard.logger.warn("Discord report channel null!");
        } catch (Exception ex) {
            ServerGuard.logger.error(ex);
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
        reportChannel.sendMessage(builder.build()).queue();
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
        reportChannel.sendMessage(message).queue();
    }

    /**
     * Retrieves the JDA instance, useful for registering events or other Discord interfaces.
     * @return {@link JDA} instance.
     */
    public static JDA getJDA() {
        return bot;
    }
}
