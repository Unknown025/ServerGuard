package org.rainyville.serverguard.server.permission;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.server.permission.context.IContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public enum DefaultPermissionHandler implements IPermissionHandler {
    INSTANCE;
    private static final HashMap<String, DefaultPermissionLevel> PERMISSION_LEVEL_MAP = new HashMap<>();
    private static final HashMap<String, String> DESCRIPTION_MAP = new HashMap<>();

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

    /**
     * @return The default permission level of a node. If the permission isn't registered, it will return NONE.
     */
    public DefaultPermissionLevel getDefaultPermissionLevel(String node) {
        DefaultPermissionLevel level = PERMISSION_LEVEL_MAP.get(node);
        return level == null ? DefaultPermissionLevel.NONE : level;
    }
}
