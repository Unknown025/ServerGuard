package org.rainyville.serverguard.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldSettings;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionAPI;

import java.util.ArrayList;
import java.util.List;

public class CommandGameMode extends PermissionCommandBase {

    public CommandGameMode() {
        PermissionAPI.getPermissionHandler().registerNode(getPermissionNode(), DefaultPermissionLevel.OP, "Grants gamemode access.");
    }

    @Override
    public String getPermissionNode() {
        return "minecraft.command.gamemode";
    }

    @Override
    public String getCommandName() {
        return "gamemode";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.gamemode.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            WorldSettings.GameType gameType = this.getGameModeFromCommand(sender, args[0]);
            EntityPlayerMP player = args.length >= 2 ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
            player.setGameType(gameType);
            player.fallDistance = 0.0F;
            ChatComponentTranslation translation = new ChatComponentTranslation("gameMode." + gameType.getName());

            if (player != sender) {
                notifyOperators(sender, this, 1, "commands.gamemode.success.other", player.getCommandSenderName(), translation);
            } else {
                notifyOperators(sender, this, 1, "commands.gamemode.success.self", translation);
            }
        } else {
            throw new WrongUsageException("commands.gamemode.usage");
        }
    }

    @Override
    public List getCommandAliases() {
        return new ArrayList<String>() {{
            add("gm");
        }};
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "survival", "creative", "adventure", "0", "1", "2") : (args.length == 2 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null);
    }

    /**
     * Gets the Game Mode specified in the command.
     */
    private WorldSettings.GameType getGameModeFromCommand(ICommandSender sender, String mode) {
        return !mode.equalsIgnoreCase(WorldSettings.GameType.SURVIVAL.getName()) &&
                !mode.equalsIgnoreCase("s") ? (!mode.equalsIgnoreCase(WorldSettings.GameType.CREATIVE.getName()) &&
                !mode.equalsIgnoreCase("c") ? (!mode.equalsIgnoreCase(WorldSettings.GameType.ADVENTURE.getName()) &&
                !mode.equalsIgnoreCase("a") ? WorldSettings.getGameTypeById(parseIntBounded(sender, mode, 0, WorldSettings.GameType.values().length - 2)) : WorldSettings.GameType.ADVENTURE) : WorldSettings.GameType.CREATIVE) : WorldSettings.GameType.SURVIVAL;
    }
}
