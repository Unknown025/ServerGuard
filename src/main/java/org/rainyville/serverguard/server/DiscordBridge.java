package org.rainyville.serverguard.server;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageChannel;
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
    private static String token = null;
    private static JDA bot = null;
    private static MessageChannel reportChannel = null;

    public static void initialize(String token) {
        DiscordBridge.token = token;
        if (token == null) return;
        try {
            bot = new JDABuilder(token).build();
            ServerGuard.logger.info("Discord bot invite link: " + bot.getInviteUrl(Permission.ADMINISTRATOR));
        } catch (LoginException e) {
            ServerGuard.logger.error("Exception when initializing DiscordBridge!", e);
        }
    }

    public static void initialize(String token, String reportChannelId) {
        initialize(token);
        if (bot == null) return;
        reportChannel = bot.getTextChannelById(reportChannelId);
    }

    public static void reportPlayer(EntityPlayerMP reported, EntityPlayerMP originator, String reason) {
        if (reportChannel == null) return;
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(originator.getCommandSenderName());
        builder.setTitle(reported.getCommandSenderName() + " Reported");
        builder.setColor(Color.ORANGE);
        builder.setDescription(reason);
        reportChannel.sendMessage(builder.build()).queue();
    }
}
