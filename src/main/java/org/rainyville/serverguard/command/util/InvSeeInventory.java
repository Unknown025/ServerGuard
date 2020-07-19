package org.rainyville.serverguard.command.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import org.rainyville.serverguard.command.CommandInventorySee;

public class InvSeeInventory extends InventoryBasic {
    public final EntityPlayerMP owner;
    public final EntityPlayerMP viewer;
    public boolean allowUpdate;

    public InvSeeInventory(EntityPlayerMP owner, EntityPlayerMP viewer) {
        super(owner.getCommandSenderName() + "'s Inventory", true, owner.inventory.mainInventory.length);
        this.owner = owner;
        this.viewer = viewer;
    }

    @Override
    public void openChest() {
        CommandInventorySee.openInventories.put(owner, this);

        allowUpdate = false;
        for (int id = 0; id < owner.inventory.mainInventory.length; ++id) {
            setInventorySlotContents(id, owner.inventory.mainInventory[id]);
        }
        allowUpdate = true;
    }

    @Override
    public void closeChest() {
        CommandInventorySee.openInventories.remove(owner);
        if (allowUpdate) {
            for (int id = 0; id < owner.inventory.mainInventory.length; ++id) {
                owner.inventory.mainInventory[id] = getStackInSlot(id);
            }
        }
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (allowUpdate) {
            for (int id = 0; id < owner.inventory.mainInventory.length; ++id) {
                owner.inventory.mainInventory[id] = getStackInSlot(id);
            }
        }
    }

    public void update() {
        allowUpdate = false;
        for (int id = 0; id < owner.inventory.mainInventory.length; ++id) {
            setInventorySlotContents(id, owner.inventory.mainInventory[id]);
        }
        allowUpdate = true;
        markDirty();
    }
}
