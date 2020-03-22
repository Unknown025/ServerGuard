package org.rainyville.serverguard.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.util.*;

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
        return "/pex <user : group>";
    }

    @Override
    public String getCommandName() {
        return "pex";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            throw new WrongUsageException("Invalid command syntax.");
        }
        if (!(PermissionAPI.getPermissionHandler() instanceof ServerGuardPermissionHandler)) {
            throw new CommandException("Wrong permission handler.");
        }
        ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
        if (args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("users")) {
            if (args.length == 1) {
                sender.addChatMessage(new ChatComponentText("Currently registered users:"));
                HashMap<UUID, ServerGuardPermissionHandler.Player> playerHashMap = handler.getRegisteredPlayers();
                for (Map.Entry<UUID, ServerGuardPermissionHandler.Player> set : playerHashMap.entrySet()) {
                    sender.addChatMessage(new ChatComponentText(set.getKey() +
                            " (Last known username: " + set.getValue().username + ")" + EnumChatFormatting.GREEN +
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
                sender.addChatMessage(new ChatComponentText(username + "'s permissions:"));
                for (int i = 0; i < player.permissions.size(); i++) {
                    String permission = player.permissions.get(i);
                    sender.addChatMessage(new ChatComponentText(String.format("%d) %s", (i + 1), permission)));
                }
                return;
            } else if (args.length == 4 || args.length == 5) {
                String username = args[1];
                String permission = args[3];

                EntityPlayer entityPlayer = PlayerSelector.matchOnePlayer(sender, username);
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
                        throw new WrongUsageException("Invalid command syntax.");
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
                    throw new WrongUsageException("Invalid command syntax.");
                }
            }
        } else if (args[0].equalsIgnoreCase("group") || args[0].equalsIgnoreCase("groups")) {
            if (args.length == 1) {
                sender.addChatMessage(new ChatComponentText("Registered groups:"));
                Set<String> playerHashMap = handler.getRegisteredGroups();
                for (String group : playerHashMap) {
                    sender.addChatMessage(new ChatComponentText(group));
                }
            } else if (args.length == 2) {
                String groupName = args[1];
                ServerGuardPermissionHandler.Group group = handler.getGroup(groupName);
                if (group == null) {
                    sender.addChatMessage(new ChatComponentText("Group \"" + groupName + "\"'s permissions:"));
                    sender.addChatMessage(new ChatComponentText("   none"));
                    return;
                }
                sender.addChatMessage(new ChatComponentText("Group \"" + groupName + "\"'s permissions:"));
                if (group.permissions.size() == 0) {
                    sender.addChatMessage(new ChatComponentText("   none"));
                } else {
                    for (int i = 0; i < group.permissions.size(); i++) {
                        String permission = group.permissions.get(i);
                        sender.addChatMessage(new ChatComponentText(String.format("%d) " + permission, (i + 1))));
                    }
                }
            } else if (args.length == 4) {
                String groupName = args[1];
                String permission = args[3];

                if (args[2].equalsIgnoreCase("add")) {
                    handler.addPermissionToGroup(groupName, permission);
                    sender.addChatMessage(new ChatComponentText("Added \"" + permission + "\" to " + groupName + "!"));
                } else if (args[2].equalsIgnoreCase("remove")) {
                    handler.removePermissionFromGroup(groupName, permission);
                    sender.addChatMessage(new ChatComponentText("Removed \"" + permission + "\" from " + groupName + "!"));
                } else {
                    throw new WrongUsageException("/pex <user : group>");
                }
                return;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            handler.reloadConfig();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Configuration file reloaded!"));
            return;
        }
        throw new SyntaxErrorException("Invalid command syntax.");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (!(PermissionAPI.getPermissionHandler() instanceof ServerGuardPermissionHandler)) {
            throw new CommandException("Wrong permission handler.");
        }
        ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
        List<String> list = new ArrayList<>();
        if (args.length <= 1) {
            list.add("group");
            list.add("user");
        } else if (args.length <= 2) {
            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        } else if (args.length <= 3) {
            list.add("add");
            if (args[0].equalsIgnoreCase("user")) {
                list.add("group");
            }
        } else if (args.length <= 4) {
            if (args[0].equalsIgnoreCase("user") && args[3].equalsIgnoreCase("group")) {
                list.add("add");
            } else {
                list.addAll(handler.getRegisteredNodes());
            }
        } else if (args.length <= 5 && args[4].equalsIgnoreCase("group")) {
            list.addAll(handler.getRegisteredGroups());
        }
        return getListOfStringsMatchingLastWord(args, list.toArray(new String[0]));
    }
}
