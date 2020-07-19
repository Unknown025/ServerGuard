package org.rainyville.serverguard.command;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.server.MinecraftServer;
import org.rainyville.serverguard.command.util.InvSeeInventory;
import org.rainyville.serverguard.server.permission.DefaultPermissionLevel;
import org.rainyville.serverguard.server.permission.PermissionCommandBase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CommandInventorySee extends PermissionCommandBase {
    public static HashMap<EntityPlayer, InvSeeInventory> openInventories = new HashMap<>();

    @Override
    public DefaultPermissionLevel getPermissionLevel() {
        return DefaultPermissionLevel.OP;
    }

    @Override
    public String getDescription() {
        return "Allows the user to see another player's inventory.";
    }

    @Override
    public String getPermissionNode() {
        return "protectguard.command.invsee";
    }

    @Override
    public String getCommandName() {
        return "inventorysee";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/inventorysee <player>";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getCommandAliases() {
        return Collections.singletonList("invsee");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1)
            throw new WrongUsageException(getCommandUsage(sender));
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        EntityPlayerMP target = getPlayer(sender, args[0]);

        if (player.openContainer != player.inventoryContainer)
            player.closeScreen();
        player.getNextWindowId();

        InvSeeInventory inventory = new InvSeeInventory(target, player);

        player.playerNetServerHandler.sendPacket(new S2DPacketOpenWindow(player.currentWindowId, 0,
                inventory.getInventoryName(), inventory.getSizeInventory(), true));
        player.openContainer = new ContainerChest(player.inventory, inventory);
        player.openContainer.windowId = player.currentWindowId;
        player.openContainer.onCraftGuiOpened(player);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }

    @SubscribeEvent
    public void tickStart(TickEvent.PlayerTickEvent event) {
        if (openInventories.containsKey(event.player)) {
            openInventories.get(event.player).update();
        }
    }
}
