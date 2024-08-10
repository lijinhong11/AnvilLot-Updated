package dev.qruet.anvillot.nms.v1_18_R2;

import dev.qruet.anvillot.nms.IItemStackWrapper;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;

public class ItemStackWrapper implements IItemStackWrapper {

    private final ItemStack itemStack;

    public ItemStackWrapper(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public int getRepairCost() {
        return itemStack.G();
    }

    @Override
    public void setRepairCost(int val) {
        itemStack.c(val);
    }

    @Override
    public org.bukkit.inventory.ItemStack getBukkitCopy() {
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public boolean isEmpty() {
        return itemStack.b();
    }

    @Override
    public String getName() {
        return itemStack.w().getString();
    }

    @Override
    public Object getNMS() {
        return itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
