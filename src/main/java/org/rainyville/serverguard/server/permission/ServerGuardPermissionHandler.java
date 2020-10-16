package org.rainyville.serverguard.server.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.ServerGuard;
import org.rainyville.serverguard.server.permission.context.IContext;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

public class ServerGuardPermissionHandler implements IPermissionHandler {
    private static final HashMap<String, DefaultPermissionLevel> PERMISSION_LEVEL_MAP = new HashMap<>();
    private static final HashMap<String, String> DESCRIPTION_MAP = new HashMap<>();
    private static HashMap<UUID, Player> PLAYER_PERMISSION_MAP = new HashMap<>();
    private static HashMap<String, Group> GROUP_PERMISSION_MAP = new HashMap<>();
    private static File CONFIG_FILE;

    public ServerGuardPermissionHandler(File configFile) {
        CONFIG_FILE = configFile;
        reloadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(ServerGuardPermissionHandler::saveConfig));
    }

    @SuppressWarnings("unchecked")
    public HashMap<UUID, Player> getRegisteredPlayers() {
        return (HashMap<UUID, Player>) PLAYER_PERMISSION_MAP.clone();
    }

    /**
     * Registers a new player in the configuration file.
     *
     * @param entityPlayer EntityPlayer to register.
     */
    public void registerPlayer(EntityPlayer entityPlayer) {
        Player player = new Player(entityPlayer.getGameProfile().getName());
        PLAYER_PERMISSION_MAP.putIfAbsent(entityPlayer.getUniqueID(), player);
    }

    /**
     * Reloads the configuration file.
     */
    public void reloadConfig() {
        Gson gson = new Gson();
        try {
            FileReader input = new FileReader(CONFIG_FILE);
            JsonReader reader = new JsonReader(input);
            Config config = gson.fromJson(reader, Config.class);
            PLAYER_PERMISSION_MAP = config.players;
            GROUP_PERMISSION_MAP = config.groups;
        } catch (Exception ex) {
            ServerGuard.logger.error(ex);
        } finally {
            if (PLAYER_PERMISSION_MAP == null)
                PLAYER_PERMISSION_MAP = new HashMap<>();
            if (GROUP_PERMISSION_MAP == null)
                GROUP_PERMISSION_MAP = new HashMap<>();
        }
    }

    /**
     * Saves the configuration file.
     */
    private static void saveConfig() {
        try {
            Writer writer = new FileWriter(CONFIG_FILE);
            Gson json = new GsonBuilder().setPrettyPrinting().create();
            Config config = new Config();
            config.players = PLAYER_PERMISSION_MAP;
            config.groups = GROUP_PERMISSION_MAP;
            json.toJson(config, writer);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ServerGuard.logger.error(ex);
        }
    }

    @Override
    public void registerNode(String node, DefaultPermissionLevel level, String desc) {
        PERMISSION_LEVEL_MAP.put(node, level);

        if (!desc.isEmpty()) {
            DESCRIPTION_MAP.put(node, desc);
        }
    }

    @Override
    public Collection<String> getRegisteredNodes() {
        return Collections.unmodifiableSet(PERMISSION_LEVEL_MAP.keySet());
    }

    @Override
    public boolean hasPermission(GameProfile profile, String node, @Nullable IContext context) {
        DefaultPermissionLevel level = getDefaultPermissionLevel(node);

        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player != null) {
            boolean canExecute = player.hasPermission(node);
            // -permission.node overrides any other permission, player cannot execute this command.
            if (player.hasPermission("-" + node)) {
                return false;
            }

            if (player.groups.size() > 0) {
                for (String groupName : player.groups) {
                    Group group = getGroup(groupName);
                    if (group == null) continue;
                    // -permission.node overrides any other permission, player cannot execute this command.
                    if (group.hasPermission("-" + node)) {
                        return false;
                    }
                }

                for (String groupName : player.groups) {
                    Group group = getGroup(groupName);
                    if (group == null) continue;
                    if (group.hasPermission(node)) {
                        canExecute = true;
                    }
                }
            }
            if (canExecute)
                return true;
        }

        if (level == DefaultPermissionLevel.NONE) {
            return false;
        } else if (level == DefaultPermissionLevel.ALL) {
            return true;
        }

        MinecraftServer server = MinecraftServer.getServer();
        return server != null && server.getConfigurationManager().canSendCommands(profile);
    }

    @Override
    public String getNodeDescription(String node) {
        String desc = DESCRIPTION_MAP.get(node);
        return desc == null ? "" : desc;
    }

    public DefaultPermissionLevel getDefaultPermissionLevel(String node) {
        DefaultPermissionLevel level = PERMISSION_LEVEL_MAP.get(node);
        return level == null ? DefaultPermissionLevel.NONE : level;
    }

    @Nullable
    public Group getGroup(String name) {
        return GROUP_PERMISSION_MAP.get(name);
    }

    @Nullable
    public Group removeGroup(String name) {
        return GROUP_PERMISSION_MAP.remove(name);
    }

    @Nullable
    public Player getPlayer(String name) {
        for (Player player : PLAYER_PERMISSION_MAP.values()) {
            if (player.username.equalsIgnoreCase(name)) {
                return player;
            }
        }
        GameProfile player = MinecraftServer.getServer().getPlayerProfileCache().getGameProfileForUsername(name);
        if (player != null)
            return PLAYER_PERMISSION_MAP.get(player.getId());
        return null;
    }

    @Nullable
    public Player getPlayer(EntityPlayer player) {
        return PLAYER_PERMISSION_MAP.get(player.getUniqueID());
    }

    @Nullable
    public Player getPlayer(UUID uuid) {
        return PLAYER_PERMISSION_MAP.get(uuid);
    }

    @Nullable
    public Player removePlayer(Player player) {
        UUID uuid = null;
        for (Map.Entry<UUID, Player> set : PLAYER_PERMISSION_MAP.entrySet()) {
            if (set.getValue() == player) {
                uuid = set.getKey();
                break;
            }
        }
        return removePlayer(uuid);
    }

    @Nullable
    public Player removePlayer(UUID uuid) {
        return PLAYER_PERMISSION_MAP.remove(uuid);
    }

    /**
     * Adds a permission to a player.
     *
     * @param profile    GameProfile of the player.
     * @param permission Permission node.
     **/
    public void addPermissionToPlayer(GameProfile profile, String permission) {
        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player == null) {
            player = new Player(profile.getName());
        } else if (player.permissions == null) {
            player.permissions = new ArrayList<>();
        }
        player.permissions.add(permission);
        PLAYER_PERMISSION_MAP.put(profile.getId(), player);
        saveConfig();
    }

    /**
     * Removes a permission from a player.
     *
     * @param profile    GameProfile of the player.
     * @param permission Permission node.
     */
    public void removePermissionFromPlayer(GameProfile profile, String permission) {
        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player == null) {
            player = new Player(profile.getName());
        } else if (player.permissions == null) {
            player.permissions = new ArrayList<>();
        }
        player.permissions.remove(permission);
        PLAYER_PERMISSION_MAP.put(profile.getId(), player);
        saveConfig();
    }

    /**
     * Adds a permission to a group.
     *
     * @param group      Group name.
     * @param permission Permission node.
     */
    public void addPermissionToGroup(String group, String permission) {
        Group savedGroup = getGroup(group);
        if (savedGroup == null)
            savedGroup = new Group();
        else if (savedGroup.permissions == null)
            savedGroup.permissions = new ArrayList<>();
        savedGroup.permissions.add(permission);
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    /**
     * Removes a permission from a group.
     *
     * @param group      Group name.
     * @param permission Permission node.
     */
    public void removePermissionFromGroup(String group, String permission) {
        Group savedGroup = getGroup(group);
        if (savedGroup == null)
            return;
        savedGroup.permissions.remove(permission);
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    /**
     * Adds a group to a player.
     *
     * @param profile   GameProfile of the player.
     * @param groupName Group name.
     */
    public void addGroupToPlayer(GameProfile profile, String groupName) {
        Group group = getGroup(groupName);
        if (group == null) {
            group = new Group();
            GROUP_PERMISSION_MAP.put(groupName, group);
        }
        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player == null) {
            player = new Player(profile.getName());
        } else if (player.groups == null) {
            player.groups = new ArrayList<>();
        }
        player.groups.add(groupName);
        PLAYER_PERMISSION_MAP.put(profile.getId(), player);
        saveConfig();
    }

    /**
     * Removes a group from a player.
     *
     * @param profile   GameProfile of the player.
     * @param groupName Group name.
     */
    public void removeGroupFromPlayer(GameProfile profile, String groupName) {
        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player == null) {
            player = new Player(profile.getName());
        } else if (player.groups == null) {
            player.groups = new ArrayList<>();
        }
        player.groups.remove(groupName);
        PLAYER_PERMISSION_MAP.put(profile.getId(), player);
        saveConfig();
    }

    public Set<String> getRegisteredGroups() {
        return GROUP_PERMISSION_MAP.keySet();
    }

    public static class Player {
        public String username;
        public List<String> permissions;
        public List<String> groups;
        public Date lastSeen;

        public Player(String name) {
            this.username = name;
            this.permissions = new ArrayList<>();
            this.groups = new ArrayList<>();
            this.groups.add("default");
            this.lastSeen = new Date();
        }

        /**
         * Checks if this player has permission to use a command.
         *
         * @param node Command permission node.
         * @return Returns if the player has permission.
         */
        public boolean hasPermission(String node) {
            for (String selfNode : permissions) {
                if (selfNode.equalsIgnoreCase(node)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Group {
        public List<String> permissions;

        public Group() {
            this.permissions = new ArrayList<>();
        }

        /**
         * Checks if this group has permission to use a command.
         *
         * @param node Command permission node.
         * @return Returns if the group has permission.
         */
        public boolean hasPermission(String node) {
            for (String selfNode : permissions) {
                if (selfNode.equalsIgnoreCase(node)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns the default group, which is automatically assigned to new users.
         *
         * @return Returns the default group.
         * @deprecated Don't need this anymore.
         */
        @Deprecated
        public static Group getDefault() {
            Group group = GROUP_PERMISSION_MAP.get("default");
            if (group == null) {
                group = new Group();
                GROUP_PERMISSION_MAP.put("default", group);
            }
            return group;
        }
    }

    static class Config {
        public HashMap<UUID, Player> players;
        public HashMap<String, Group> groups;
    }
}
