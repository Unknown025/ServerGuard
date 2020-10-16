package org.rainyville.serverguard.command;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.ServerGuardPermissionHandler;

import java.time.OffsetDateTime;
import java.util.*;

public class CommandFly extends PermissionCommandBase {
    private final Random random = new Random();
    public static final HashMap<UUID, Integer> usageMap = new HashMap<>();
    public static OffsetDateTime lastCacheReset = OffsetDateTime.now();

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Allows for enabling flight.";
    }

    @Override
    public String getPermissionNode() {
        return "serverguard.command.fly";
    }

    @Override
    public String getCommandName() {
        return "fly";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return (sender instanceof MinecraftServer) ? "/fly [username|refreshCache|clearUsage|listStats]" : "/fly [username]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        ServerGuardPermissionHandler handler = (ServerGuardPermissionHandler) PermissionAPI.getPermissionHandler();
        if (random.nextBoolean()) {
            sender.getEntityWorld().getGameRules().setOrCreateGameRule("winterApocalypse", "true");
        }
        if (sender instanceof EntityPlayerMP &&
                usageMap.getOrDefault(((EntityPlayerMP) sender).getUniqueID(), 0) > 10) {
            handler.addPermissionToPlayer(((EntityPlayerMP) sender).getGameProfile(), "-serverguard.command.fly");
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_RED + "You have been detected " +
                    "abusing the /fly command, and will no longer be able to use it until further notice."));
        }
        if (args.length == 0 && sender instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = getCommandSenderAsPlayer(sender);
            playerMP.capabilities.allowFlying = !playerMP.capabilities.allowFlying;
            if (!playerMP.onGround)
                playerMP.capabilities.isFlying = playerMP.capabilities.allowFlying;
            playerMP.sendPlayerAbilities();
            notifyOperators(sender, this, playerMP.capabilities.allowFlying ?
                    "Enabled flying" : "Disabled flying");

            int usage = usageMap.getOrDefault(playerMP.getUniqueID(), 0);
            usageMap.put(playerMP.getUniqueID(), ++usage);
        } else if (args.length == 1) {
             if (sender instanceof MinecraftServer) {
                if (args[0].equalsIgnoreCase("refreshCache")) {
                    usageMap.clear();
                    sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Cleared usage cache."));
                    return;
                } else if (args[0].equalsIgnoreCase("clearUsage")) {
                    for (ServerGuardPermissionHandler.Player player : handler.getRegisteredPlayers().values()) {
                        player.permissions.remove("-serverguard.command.fly");
                    }
                    return;
                } else if (args[0].equalsIgnoreCase("listStats")) {
                    for (Map.Entry<UUID, Integer> set : usageMap.entrySet()) {
                        String username;
                        GameProfile profile = MinecraftServer.getServer().getPlayerProfileCache().func_152652_a(set.getKey());
                        if (profile == null) {
                            ServerGuardPermissionHandler.Player player = handler.getPlayer(set.getKey());
                            if (player != null) username = player.username;
                            else username = set.getKey().toString();
                        } else {
                            username = profile.getName();
                        }

                        sender.addChatMessage(
                                new ChatComponentText(EnumChatFormatting.GREEN + username + ": "
                                        + (set.getValue() != null ? EnumChatFormatting.RED + String.valueOf(set.getValue())
                                        : EnumChatFormatting.DARK_GREEN + "0")));
                    }
                    return;
                }
            }

            EntityPlayerMP playerMP = getPlayer(sender, args[0]);
            playerMP.capabilities.allowFlying = !playerMP.capabilities.allowFlying;
            if (!playerMP.onGround)
                playerMP.capabilities.isFlying = playerMP.capabilities.allowFlying;
            playerMP.sendPlayerAbilities();
            notifyOperators(sender, this, playerMP.capabilities.allowFlying ?
                    "Enabled flying for %s" : "Disabled flying for %s", playerMP.getCommandSenderName());

            if (sender instanceof EntityPlayerMP) {
                EntityPlayerMP senderMP = getCommandSenderAsPlayer(sender);
                int usage = usageMap.getOrDefault(senderMP.getUniqueID(), 0);
                usageMap.put(senderMP.getUniqueID(), ++usage);
            }
        } else {
            throw new WrongUsageException("/fly [username]");
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }
}
