package org.rainyville.serverguard.server.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.ServerGuard;
import org.rainyville.serverguard.server.permission.context.IContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class ServerGuardPermissionHandler implements IPermissionHandler {
    public static final String DEFAULT_GROUP = "default";

    private static final HashMap<Class<? extends ICommand>, String> COMMAND_NODES_MAP = new HashMap<>();
    private static final HashMap<String, DefaultPermissionLevel> PERMISSION_LEVEL_MAP = new HashMap<>();
    private static final HashMap<String, String> DESCRIPTION_MAP = new HashMap<>();
    private static final HashMap<UUID, Player> PLAYER_PERMISSION_MAP = new HashMap<>();
    private static HashMap<String, Group> GROUP_PERMISSION_MAP = new HashMap<>();
    private static File CONFIG_FILE;

    public ServerGuardPermissionHandler(File configFile) {
        CONFIG_FILE = configFile;
        reloadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(ServerGuardPermissionHandler::saveConfig));
    }

    /**
     * Gets the permission node for a command class.
     *
     * @param clazz Command class.
     * @return Returns the permission node.
     */
    public String getCommandNode(Class<? extends ICommand> clazz) {
        return COMMAND_NODES_MAP.get(clazz);
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
        Player player = PLAYER_PERMISSION_MAP.containsKey(entityPlayer.getUniqueID()) ? PLAYER_PERMISSION_MAP.get(entityPlayer.getUniqueID()) : new Player(entityPlayer.getGameProfile());
        if (!Objects.equals(player.getUsername(), entityPlayer.getGameProfile().getName())) {
            player.updateUsername(entityPlayer.getGameProfile().getName());
        } else if (player.usernames.isEmpty()) {
            player.usernames.add(entityPlayer.getGameProfile().getName());
        }
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

            GROUP_PERMISSION_MAP = config.groups;

            PLAYER_PERMISSION_MAP.clear();
            for (Map.Entry<UUID, InternalPlayer> entry : config.players.entrySet()) {
                PLAYER_PERMISSION_MAP.put(entry.getKey(), entry.getValue().setUUID(entry.getKey()).toPlayer());
            }

            // Set group names.
            for (Map.Entry<String, Group> entry : GROUP_PERMISSION_MAP.entrySet())
                entry.getValue().setName(entry.getKey());
        } catch (Exception ex) {
            ServerGuard.logger.error("Exception while reloading the config", ex);
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
            config.players = new HashMap<>();
            for (Map.Entry<UUID, Player> entry : PLAYER_PERMISSION_MAP.entrySet()) {
                config.players.put(entry.getKey(), entry.getValue().toInternal());
            }
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

            if (!player.groups.isEmpty()) {
                for (String groupName : player.getGroupNames()) {
                    Group group = getGroup(groupName);
                    if (group == null) continue;
                    // -permission.node overrides any other permission, player cannot execute this command.
                    if (group.hasPermission("-" + node)) {
                        return false;
                    }
                }

                for (String groupName : player.getGroupNames()) {
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
    public String getNodeDescription(@Nonnull String node) {
        String desc = DESCRIPTION_MAP.get(node);
        return desc == null ? "" : desc;
    }

    public DefaultPermissionLevel getDefaultPermissionLevel(String node) {
        DefaultPermissionLevel level = PERMISSION_LEVEL_MAP.get(node);
        return level == null ? DefaultPermissionLevel.OP : level;
    }

    @Nullable
    public Group getGroup(String name) {
        return GROUP_PERMISSION_MAP.get(name);
    }

    public Group getOrCreateGroup(String name) {
        if (GROUP_PERMISSION_MAP.containsKey(name))
            return GROUP_PERMISSION_MAP.get(name);
        return new Group(name);
    }

    @Nullable
    public Group removeGroup(String name) {
        return GROUP_PERMISSION_MAP.remove(name);
    }

    @Nullable
    public Player getPlayer(String name) {
        for (Player player : PLAYER_PERMISSION_MAP.values()) {
            if (Objects.equals(player.getUsername(), name)) {
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
    public Player removePlayer(Player internalPlayer) {
        UUID uuid = null;
        for (Map.Entry<UUID, Player> set : PLAYER_PERMISSION_MAP.entrySet()) {
            if (set.getValue() == internalPlayer) {
                uuid = set.getKey();
                break;
            }
        }
        return uuid != null ? removePlayer(uuid) : null;
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
            player = new Player(profile);
        }
        if (!player.permissions.contains(permission))
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
            player = new Player(profile);
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
            savedGroup = new Group(group);
        if (!savedGroup.permissions.contains(permission))
            savedGroup.permissions.add(permission);
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    /**
     * Sets a group's prefix.
     *
     * @param group  Group name.
     * @param prefix Group prefix.
     */
    public void setGroupPrefix(String group, String prefix) {
        Group savedGroup = getGroup(group);
        if (savedGroup == null)
            savedGroup = new Group(group);
        savedGroup.prefix = prefix;
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    /**
     * Sets a group's suffix.
     *
     * @param group  Group name.
     * @param prefix Group suffix.
     */
    public void setGroupSuffix(String group, String prefix) {
        Group savedGroup = getGroup(group);
        if (savedGroup == null)
            savedGroup = new Group(group);
        savedGroup.suffix = prefix;
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    /**
     * Set's a group's rank.
     *
     * @param group Group name.
     * @param rank  Rank.
     */
    public void setGroupRank(String group, int rank) {
        Group savedGroup = getGroup(group);
        if (savedGroup == null)
            savedGroup = new Group(group);
        savedGroup.rank = rank;
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
            group = new Group(groupName);
            GROUP_PERMISSION_MAP.put(groupName, group);
        }
        Player player = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (player == null)
            player = new Player(profile);
        if (!player.groups.contains(group))
            player.groups.add(group);
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
        Player internalPlayer = PLAYER_PERMISSION_MAP.get(profile.getId());
        if (internalPlayer == null) {
            internalPlayer = new Player(profile);
        }
        Group group = getGroup(groupName);
        internalPlayer.groups.remove(group);
        PLAYER_PERMISSION_MAP.put(profile.getId(), internalPlayer);
        saveConfig();
    }

    public Set<String> getRegisteredGroups() {
        return GROUP_PERMISSION_MAP.keySet();
    }

    private static class InternalPlayer {
        public UUID uuid;
        public String username;
        public List<String> usernames;
        public List<String> permissions;
        @SerializedName("groups")
        private final List<String> groupNames;
        public Date lastSeen;

        private InternalPlayer(String username, UUID uuid) {
            this.uuid = uuid;
            this.usernames = new LinkedList<>();
            this.usernames.add(username);
            this.permissions = new ArrayList<>();
            this.groupNames = new ArrayList<>();
            this.lastSeen = new Date();
        }

        public InternalPlayer setUUID(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Player toPlayer() {
            return new Player(this);
        }
    }

    public static class Player {
        public UUID uuid;
        public String username;
        public List<String> usernames;
        public List<String> permissions;
        public Date lastSeen;
        public List<Group> groups = new ArrayList<>();

        /**
         * Create new.
         *
         * @param profile Player's GameProfile.
         */
        public Player(GameProfile profile) {
            this.uuid = profile.getId();
            this.usernames = new LinkedList<>();
            this.usernames.add(profile.getName());
            this.permissions = new ArrayList<>();
            this.lastSeen = new Date();

            this.groups.add(((ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler()).getOrCreateGroup(DEFAULT_GROUP));
        }

        /**
         * Load from cache.
         *
         * @param internal InternalPlayer.
         */
        private Player(InternalPlayer internal) {
            this.uuid = internal.uuid;
            this.usernames = new ArrayList<>();
            if (internal.usernames != null)
                this.usernames.addAll(internal.usernames.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            if (internal.username != null && !this.usernames.contains(internal.username))
                this.usernames.add(0, internal.username);
            this.permissions = new ArrayList<>();
            if (internal.permissions != null)
                this.permissions.addAll(internal.permissions);
            this.lastSeen = internal.lastSeen;

            for (String groupName : internal.groupNames) {
                Group group = GROUP_PERMISSION_MAP.get(groupName);
                if (group == null) continue;

                groups.add(group);
            }

            Collections.sort(groups);
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

        /**
         * Gets this player's current username.
         *
         * @return Returns the username.
         */
        public String getUsername() {
            return !usernames.isEmpty() ? usernames.get(0) : null;
        }

        /**
         * Updates the player's username, if applicable.
         *
         * @param username New username.
         */
        public void updateUsername(String username) {
            usernames.add(0, username);
        }

        public InternalPlayer toInternal() {
            InternalPlayer internalPlayer = new InternalPlayer(this.getUsername(), this.uuid);

            internalPlayer.permissions = this.permissions;
            internalPlayer.lastSeen = this.lastSeen;

            for (Group group : groups) {
                if (internalPlayer.groupNames.contains(group.name)) continue;

                internalPlayer.groupNames.add(group.name);
            }

            return internalPlayer;
        }

        public String[] getGroupNames() {
            String[] groups = new String[this.groups.size()];
            List<Group> groupList = this.groups;
            for (int i = 0; i < groupList.size(); i++) {
                Group group = groupList.get(i);
                groups[i] = group.name;
            }

            return groups;
        }
    }

    public static class Group implements Comparable<Group> {
        public List<String> permissions;
        @Nullable
        public Integer rank;
        public String name;

        @Nullable
        public String prefix;
        @Nullable
        public String suffix;

        public Group(String name) {
            this.name = name;
            this.permissions = new ArrayList<>();
        }

        private void setName(String name) {
            this.name = name;
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

        @Override
        public String toString() {
            return "{groupName=\"" + name + "\"}";
        }

        @Override
        public int compareTo(Group o) {
            if (o.rank == null) {
                if (this.rank == null)
                    return 0;
                return 1;
            } else if (o.rank < this.rank)
                return 1;
            return -1;
        }
    }

    static class Config {
        public HashMap<UUID, InternalPlayer> players;
        public HashMap<String, Group> groups;
    }
}
