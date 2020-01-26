package org.rainyville.serverguard.server.events;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.CommandEvent;
import org.rainyville.serverguard.command.PermissionCommandBase;
import org.rainyville.serverguard.server.permission.PermissionAPI;

public class CommandHandler {
    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        if (event.command instanceof PermissionCommandBase) {
            if (!PermissionAPI.hasPermission(event.sender, ((PermissionCommandBase) event.command).getPermissionNode())) {
                event.setCanceled(true);
                event.sender.addChatMessage(new ChatComponentTranslation("commands.generic.permission"));
            }
        }
    }
}
