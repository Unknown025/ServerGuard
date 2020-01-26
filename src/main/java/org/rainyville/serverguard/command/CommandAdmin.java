package org.rainyville.serverguard.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.List;

public class CommandAdmin extends PermissionCommandBase {
    public CommandAdmin() {
        PermissionAPI.getPermissionHandler().registerNode(getPermissionNode(), DefaultPermissionLevel.OP, "Grants admin access.");
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
        return "commands.admin.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        GameProfile profile = null;
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        if (sender instanceof EntityPlayer) {
            if (args.length > 0)
                throw new WrongUsageException("commands.admin.usage");
            profile = minecraftserver.getPlayerProfileCache().getGameProfileForUsername(sender.getCommandSenderName());
        } else if (sender instanceof MinecraftServer) {
            if (args.length != 1)
                throw new WrongUsageException("commands.admin.usage");
            profile = minecraftserver.getPlayerProfileCache().getGameProfileForUsername(args[0]);
        }
        if (profile == null)
            throw new PlayerNotFoundException();
        minecraftserver.getConfigurationManager().addOp(profile);
        notifyOperators(sender, this, "commands.admin.success", "opped");
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof MinecraftServer) {
            String s = args[args.length - 1];
            ArrayList<String> list = new ArrayList<String>();
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
