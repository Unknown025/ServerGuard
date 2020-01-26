package org.rainyville.serverguard.server.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
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
    private final File CONFIG_FILE;

    public ServerGuardPermissionHandler(File configFile) {
        CONFIG_FILE = configFile;
        reloadConfig();
    }

    @SuppressWarnings("unchecked")
    public HashMap<UUID, Player> getRegisteredPlayers() {
        return (HashMap<UUID, Player>) PLAYER_PERMISSION_MAP.clone();
    }

    private void reloadConfig() {
        Gson gson = new Gson();
        try {
            FileReader input = new FileReader(CONFIG_FILE);
            JsonReader reader = new JsonReader(input);
            Config config = gson.fromJson(reader, Config.class);
            PLAYER_PERMISSION_MAP = config.knownPlayers;
            GROUP_PERMISSION_MAP = config.knownGroups;
        } catch (Exception ex) {
            ServerGuard.logger.error(ex);
        }
    }

    private void saveConfig() {
        try {
            Writer writer = new FileWriter(CONFIG_FILE);
            Gson json = new GsonBuilder().setPrettyPrinting().create();
            Config config = new Config();
            config.knownPlayers = PLAYER_PERMISSION_MAP;
            config.knownGroups = GROUP_PERMISSION_MAP;
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
                    if (group.hasPermission(node)) {
                        canExecute = true;
                    }
                }

                for (String groupName : player.groups) {
                    Group group = getGroup(groupName);
                    if (group == null) continue;
                    // -permission.node overrides any other permission, player cannot execute this command.
                    if (group.hasPermission("-" + node)) {
                        canExecute = false;
                    }
                }
            }

            return canExecute;
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
    public Player getPlayer(String name) {
        for (Player player : PLAYER_PERMISSION_MAP.values()) {
            if (player.username.equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
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
        }
        player.selfNodes.add(permission);
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
        }
        player.selfNodes.remove(permission);
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
        savedGroup.selfNodes.add(permission);
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
        savedGroup.selfNodes.remove(permission);
        GROUP_PERMISSION_MAP.put(group, savedGroup);
        saveConfig();
    }

    public static class Player {
        public String username;
        public List<String> selfNodes;
        public List<String> groups;

        public Player(String name) {
            this.username = name;
            this.selfNodes = new ArrayList<>();
            this.groups = new ArrayList<>();
        }

        public boolean hasPermission(String node) {
            for (String selfNode : selfNodes) {
                if (selfNode.equalsIgnoreCase(node)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class Group {
        public List<String> selfNodes;

        public boolean hasPermission(String node) {
            for (String selfNode : selfNodes) {
                if (selfNode.equalsIgnoreCase(node)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class Config {
        public HashMap<UUID, Player> knownPlayers;
        public HashMap<String, Group> knownGroups;
    }
}
