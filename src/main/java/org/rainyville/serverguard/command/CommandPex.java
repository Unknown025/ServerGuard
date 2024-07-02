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
    private final ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.NONE;
    }

    @Override
    public String getDescription() {
        return "Controls ServerGuard's permission system.";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/pex <user|group>";
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
            throw new WrongUsageException("Invalid command syntax.");
        }
        if (args[0].equalsIgnoreCase("user") || args[0].equalsIgnoreCase("users")) {
            handleUsers(sender, args);
        } else if (args[0].equalsIgnoreCase("group") || args[0].equalsIgnoreCase("groups")) {
            handleGroups(sender, args);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            handler.reloadConfig();
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Configuration file reloaded!"));
        } else {
            throw new SyntaxErrorException("Invalid command syntax.");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
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

    private void handleUsers(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.addChatMessage(new ChatComponentText("Currently registered users:"));
            HashMap<UUID, ServerGuardPermissionHandler.Player> playerHashMap = handler.getRegisteredPlayers();
            for (Map.Entry<UUID, ServerGuardPermissionHandler.Player> set : playerHashMap.entrySet()) {
                sender.addChatMessage(new ChatComponentText(set.getKey() +
                        (set.getValue().getUsername() != null ? " (Last known username: " + set.getValue().getUsername() + ")" : "") + EnumChatFormatting.GREEN +
                        " [" + String.join(", ", set.getValue().getGroupNames()) + "]"));
            }
        } else if (args.length == 2 || args.length == 3) {
            String username = args[1];
            ServerGuardPermissionHandler.Player player = handler.getPlayer(username);
            if (player == null) {
                sender.addChatMessage(new ChatComponentText("Username: " + username));
                sender.addChatMessage(new ChatComponentText("No information available."));
                return;
            }
            if (args.length == 2 || args[2].equalsIgnoreCase("groups")) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Username: " + EnumChatFormatting.GREEN + player.getUsername()
                        + (!player.usernames.isEmpty() ? EnumChatFormatting.DARK_GREEN + " [" + EnumChatFormatting.WHITE + String.join(", ", player.usernames) + EnumChatFormatting.DARK_GREEN + "]" : "")));
                if (player.groups.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText("Groups: none"));
                } else {
                    sender.addChatMessage(new ChatComponentText("Groups:"));
                    for (ServerGuardPermissionHandler.Group group : player.groups) {
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + " - " + EnumChatFormatting.DARK_AQUA + group.name));
                    }
                }
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + player.getUsername() + EnumChatFormatting.GOLD + "'s permissions:"));
                for (int i = 0; i < player.permissions.size(); i++) {
                    String permission = player.permissions.get(i);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + String.format("%d) ", (i + 1))
                            + EnumChatFormatting.GRAY + permission));
                }
                if (player.permissions.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + " - " + EnumChatFormatting.GRAY + "none"));
                }
            } else if (args[2].equalsIgnoreCase("delete")) {
                player = handler.removePlayer(player);
                if (player == null)
                    throw new PlayerNotFoundException();
                sender.addChatMessage(new ChatComponentText("Removed " + player.getUsername() + "!"));
            } else {
                throw new WrongUsageException(getCommandUsage(sender));
            }

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
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Added \"" + groupName + "\" to " + username + "!"));
                } else if (args[3].equalsIgnoreCase("remove")) {
                    handler.removeGroupFromPlayer(profile, groupName);
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Removed \"" + groupName + "\" from " + username + "!"));
                } else {
                    throw new WrongUsageException("Invalid command syntax.");
                }
                return;
            }

            if (args[2].equalsIgnoreCase("add")) {
                handler.addPermissionToPlayer(profile, permission);
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Added \"" + permission + "\" to " + username + "!"));
            } else if (args[2].equalsIgnoreCase("remove")) {
                handler.removePermissionFromPlayer(profile, permission);
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Removed \"" + permission + "\" from " + username + "!"));
            } else {
                throw new WrongUsageException("Invalid command syntax.");
            }
        } else {
            throw new WrongUsageException("/pex <user|group>");
        }
    }

    private void handleGroups(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Registered groups:"));
            Set<String> playerHashMap = handler.getRegisteredGroups();
            for (String group : playerHashMap) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + " - " + EnumChatFormatting.DARK_AQUA + group));
            }
        } else if (args.length < 4) {
            String groupName = args[1];
            ServerGuardPermissionHandler.Group group = handler.getGroup(groupName);

            if (args.length == 2) {
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Group \"" + EnumChatFormatting.GREEN
                        + groupName + EnumChatFormatting.GOLD + "\"'s permissions:"));
                if (group == null || group.permissions.isEmpty()) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "   none"));
                } else {
                    for (int i = 0; i < group.permissions.size(); i++) {
                        String permission = group.permissions.get(i);
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + String.format("%d) " +
                                EnumChatFormatting.GRAY + permission, (i + 1))));
                    }
                }
            } else {
                if (args[2].equalsIgnoreCase("users") || args[2].equalsIgnoreCase("user")) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Group \"" + EnumChatFormatting.GREEN
                            + groupName + EnumChatFormatting.GOLD + "\"'s members:"));
                    int count = 1;
                    for (ServerGuardPermissionHandler.Player player : handler.getRegisteredPlayers().values()) {
                        if (player.groups.contains(group)) {
                            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + String.valueOf(count) + ") "
                                    + EnumChatFormatting.GRAY + player.getUsername()));
                            count++;
                        }
                    }
                } else if (args[2].equalsIgnoreCase("delete")) {
                    ServerGuardPermissionHandler.Group remGroup = handler.removeGroup(groupName);
                    if (remGroup != null)
                        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Removed " + groupName + "!"));
                    else
                        throw new CommandException("Group \"" + groupName + "\" not found!");
                } else if (args[2].equalsIgnoreCase("prefix")) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Prefix:"));
                    if (group == null || group.prefix == null)
                        sender.addChatMessage(new ChatComponentText("None"));
                    else
                        sender.addChatMessage(new ChatComponentText(group.prefix));
                } else if (args[2].equalsIgnoreCase("suffix")) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Suffix:"));
                    if (group == null || group.suffix == null)
                        sender.addChatMessage(new ChatComponentText("None"));
                    else
                        sender.addChatMessage(new ChatComponentText(group.prefix));
                } else if (args[2].equalsIgnoreCase("rank")) {
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Rank: " + EnumChatFormatting.AQUA
                            + (group == null || group.rank == null ? "unranked" : group.rank)));
                } else {
                    throw new WrongUsageException(getCommandUsage(sender));
                }
            }
        } else if (args.length == 4) {
            String groupName = args[1];
            String permission = args[3];

            if (args[2].equalsIgnoreCase("add")) {
                handler.addPermissionToGroup(groupName, permission);
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Added \"" + permission + "\" to " + groupName + "!"));
            } else if (args[2].equalsIgnoreCase("remove")) {
                handler.removePermissionFromGroup(groupName, permission);
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Removed \"" + permission + "\" from " + groupName + "!"));
            } else if (args[2].equalsIgnoreCase("prefix")) {
                handler.setGroupPrefix(groupName, permission);
                sender.addChatMessage(new ChatComponentText("Set group " + groupName + "'s prefix to: " + permission));
            } else if (args[2].equalsIgnoreCase("suffix")) {
                handler.setGroupSuffix(groupName, permission);
                sender.addChatMessage(new ChatComponentText("Set group " + groupName + "'s suffix to: " + permission));
            } else if (args[2].equalsIgnoreCase("rank")) {
                int rank = parseInt(sender, args[3]);
                handler.setGroupRank(groupName, rank);
                sender.addChatMessage(new ChatComponentText("Set group " + groupName + "'s rank to: " + rank));
            } else {
                throw new WrongUsageException("/pex <user|group>");
            }
        } else {
            throw new WrongUsageException("/pex <user|group>");
        }
    }
}
