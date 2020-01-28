package org.rainyville.serverguard.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.ArrayList;
import java.util.List;

public class CommandAdmin extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Grants admin permissions.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.admin";
    }

    @Override
    public String getCommandName() {
        return "admin";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/admin";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        GameProfile profile = null;
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        if (sender instanceof EntityPlayer) {
            if (args.length > 0)
                throw new SyntaxErrorException("commands.generic.syntax");
            profile = minecraftServer.getPlayerProfileCache().getGameProfileForUsername(sender.getCommandSenderName());
        } else if (sender instanceof MinecraftServer) {
            if (args.length != 1)
                throw new SyntaxErrorException("commands.generic.syntax");
            profile = minecraftServer.getPlayerProfileCache().getGameProfileForUsername(args[0]);
        }
        if (profile == null)
            throw new PlayerNotFoundException();
        boolean opped = false;
        if (minecraftServer.getConfigurationManager().canSendCommands(profile)) {
            minecraftServer.getConfigurationManager().removeOp(profile);
        } else {
            minecraftServer.getConfigurationManager().addOp(profile);
            opped = true;
        }
        if (sender instanceof MinecraftServer) {
            notifyOperators(sender, this, "Updated %s's admin status to %s.", profile.getName(), opped ? "opped" : "deopped");
        } else {
            notifyOperators(sender, this, "Updated your admin status to %s.", opped ? "opped" : "deopped");
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof MinecraftServer) {
            String s = args[args.length - 1];
            ArrayList<String> list = new ArrayList<>();
            GameProfile[] gameProfiles = MinecraftServer.getServer().getGameProfiles();

            for (GameProfile gameprofile : gameProfiles) {
                if (!MinecraftServer.getServer().getConfigurationManager().canSendCommands(gameprofile) && doesStringStartWith(s, gameprofile.getName())) {
                    list.add(gameprofile.getName());
                }
            }

            return list;
        } else {
            return null;
        }
    }
}
