package org.rainyville.serverguard.server.permission;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Logger;
import org.rainyville.serverguard.ServerGuard;
import org.rainyville.serverguard.server.permission.context.IContext;
import org.rainyville.serverguard.server.permission.context.PlayerContext;

import javax.annotation.Nullable;

public class PermissionAPI {
    private static final Logger LOGGER = ServerGuard.logger;
    private static IPermissionHandler permissionHandler = DefaultPermissionHandler.INSTANCE;

    /**
     * <b>Only use this in PreInit state!</b>
     */
    public static void setPermissionHandler(IPermissionHandler handler) {
        Preconditions.checkNotNull(handler, "Permission handler can't be null!");
        Preconditions.checkState(Loader.instance().getLoaderState().ordinal() <= LoaderState.PREINITIALIZATION.ordinal(), "Can't register after IPermissionHandler PreInit!");
        LOGGER.warn("Replacing {} with {}", permissionHandler.getClass().getName(), handler.getClass().getName());
        permissionHandler = handler;
    }

    public static IPermissionHandler getPermissionHandler() {
        return permissionHandler;
    }

    /**
     * <b>Only use this after PreInit state!</b>
     *
     * @param node  Permission node, best if it's lowercase and contains '.' (e.g. <code>"modid.subgroup.permission_id"</code>)
     * @param level Default permission level for this node. If not isn't registered, it's level is going to be 'NONE'
     * @param desc  Optional description of the node
     */
    public static String registerNode(String node, DefaultPermissionLevel level, String desc) {
        Preconditions.checkNotNull(node, "Permission node can't be null!");
        Preconditions.checkNotNull(level, "Permission level can't be null!");
        Preconditions.checkNotNull(desc, "Permission description can't be null!");
        Preconditions.checkArgument(!node.isEmpty(), "Permission node can't be empty!");
        Preconditions.checkState(Loader.instance().getLoaderState().ordinal() > LoaderState.PREINITIALIZATION.ordinal(), "Can't register permission nodes before Init!");
        permissionHandler.registerNode(node, level, desc);
        return node;
    }

    /**
     * @param profile GameProfile of the player who is requesting permission. The player doesn't have to be online
     * @param node    Permission node. See {@link #registerNode(String, DefaultPermissionLevel, String)}
     * @param context Context for this permission. Highly recommended to not be null. See {@link IContext}
     * @return true, if player has permission, false if he does not.
     * @see DefaultPermissionHandler
     */
    public static boolean hasPermission(GameProfile profile, String node, @Nullable IContext context) {
        Preconditions.checkNotNull(profile, "GameProfile can't be null!");
        Preconditions.checkNotNull(node, "Permission node can't be null!");
        Preconditions.checkArgument(!node.isEmpty(), "Permission node can't be empty!");
        return permissionHandler.hasPermission(profile, node, context);
    }

    /**
     * Shortcut method using EntityPlayer and creating PlayerContext
     *
     * @see PermissionAPI#hasPermission(GameProfile, String, IContext)
     */
    public static boolean hasPermission(EntityPlayer player, String node) {
        Preconditions.checkNotNull(player, "Player can't be null!");
        return hasPermission(player.getGameProfile(), node, new PlayerContext(player));
    }

    /**
     * Shortcut method using ICommandSender.
     * @param sender Any ICommandSender.
     * @param node Node to check for.
     * @return Returns if the sender has permission.
     */
    public static boolean hasPermission(ICommandSender sender, String node) {
        Preconditions.checkNotNull(sender, "Sender can't be null!");
        if (sender instanceof EntityPlayer) {
            return hasPermission(((EntityPlayer) sender).getGameProfile(), node, new PlayerContext((EntityPlayer) sender));
        }
        return true;
    }
}
