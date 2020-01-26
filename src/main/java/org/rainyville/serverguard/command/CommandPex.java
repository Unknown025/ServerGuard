package org.rainyville.serverguard.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandPex extends PermissionCommandBase {
    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.NONE;
    }

    @Override
    public String getDescription() {
        return "Controls ServerGuard's permission system.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.pex";
    }

    @Override
    public String getCommandName() {
        return "pex";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException("commands.pex.usage");
        }
        if (!(PermissionAPI.getPermissionHandler() instanceof ServerGuardPermissionHandler)) {
            throw new CommandException("commands.pex.wrongpermissionhandler");
        }
        ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
        if (args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("users")) {
            if (args.length == 1) {
                HashMap<UUID, ServerGuardPermissionHandler.Player> playerHashMap = handler.getRegisteredPlayers();
                for (Map.Entry<UUID, ServerGuardPermissionHandler.Player> set : playerHashMap.entrySet()) {
                    sender.addChatMessage(new ChatComponentText(set.getKey() +
                            " (last known username: " + set.getValue().username + ")" + EnumChatFormatting.GREEN +
                            " [" + String.join(", ", set.getValue().groups) + "]"));
                }
            } else if (args.length == 2) {
                String username = args[1];
                ServerGuardPermissionHandler.Player player = handler.getPlayer(username);
                if (player == null) {
                    sender.addChatMessage(new ChatComponentText("Username: " + username));
                    sender.addChatMessage(new ChatComponentText("No information available."));
                    return;
                }
                sender.addChatMessage(new ChatComponentText("Username: " + player.username));
                if (player.groups.size() == 0) {
                    sender.addChatMessage(new ChatComponentText("Groups: none"));
                } else {
                    sender.addChatMessage(new ChatComponentText("Groups:"));
                    for (String group : player.groups) {
                        sender.addChatMessage(new ChatComponentText(" - " + group));
                    }
                }
                if (player.selfNodes.size() == 0) {
                    sender.addChatMessage(new ChatComponentText("Own permissions: none"));
                } else {
                    sender.addChatMessage(new ChatComponentText("Own permissions: "));
                    for (String node : player.selfNodes) {
                        sender.addChatMessage(new ChatComponentText(" - " + node));
                    }
                }
            } else if (args.length == 4 || args.length == 5) {
                String username = args[1];
                String permission = args[3];

                EntityPlayer entityPlayer = getPlayer(sender, username);
                GameProfile profile;
                if (entityPlayer != null) {
                    profile = entityPlayer.getGameProfile();
                } else {
                    profile = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(username);
                }
                if (profile == null) {
                    throw new PlayerNotFoundException();
                }

                if (args[2].equalsIgnoreCase("group")) {
                    String groupName = args[4];
                    if (args[3].equalsIgnoreCase("add")) {
                        handler.addGroupToPlayer(profile, groupName);
                        sender.addChatMessage(new ChatComponentText("Added \"" + groupName + "\" to " + username + "!"));
                    } else if (args[3].equalsIgnoreCase("remove")) {
                        handler.removeGroupFromPlayer(profile, groupName);
                        sender.addChatMessage(new ChatComponentText("Removed \"" + groupName + "\" from " + username + "!"));
                    } else {
                        throw new WrongUsageException("commands.pex.usage");
                    }
                    return;
                }

                if (args[2].equalsIgnoreCase("add")) {
                    handler.addPermissionToPlayer(profile, permission);
                    sender.addChatMessage(new ChatComponentText("Added \"" + permission + "\" to " + username + "!"));
                } else if (args[2].equalsIgnoreCase("remove")) {
                    handler.removePermissionFromPlayer(profile, permission);
                    sender.addChatMessage(new ChatComponentText("Removed \"" + permission + "\" from " + username + "!"));
                } else {
                    throw new WrongUsageException("commands.pex.usage");
                }
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            handler.reloadConfig();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Configuration file reloaded!"));
        }
    }
}
