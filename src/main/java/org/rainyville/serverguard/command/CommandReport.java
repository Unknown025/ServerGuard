package org.rainyville.serverguard.command;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.rainyville.serverguard.server.DiscordBridge;

import javax.annotation.Nullable;
import java.util.List;

public class CommandReport extends CommandBase {
    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/report <player> <reason>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP report = getPlayer(server, sender, args[0]);
        EntityPlayerMP originator = getCommandSenderAsPlayer(sender);

        if (report.getGameProfile().equals(originator.getGameProfile())) {
            throw new WrongUsageException("Cannot report yourself!");
        }

        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]);
            reason.append(" ");
        }
        DiscordBridge.reportPlayer(report, originator, reason.toString().trim());
        originator.sendMessage(new TextComponentString(TextFormatting.DARK_GREEN + "Reported " +
                TextFormatting.RED + report.getName() + TextFormatting.DARK_GREEN + "!"));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
    }
}
