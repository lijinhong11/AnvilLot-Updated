package dev.qruet.anvillot.nms.v1_19_R3;

import org.bukkit.block.Block;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.ITileInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;

/**
 * @author lijinhong
 * @version 3.7.0
 */
public class AnvilLotTileInventory implements ITileInventory {

    private final IChatBaseComponent a;
    private final BlockPosition pos;

    public AnvilLotTileInventory(Block block, IChatBaseComponent var1) {
        this.a = var1;
        this.pos = ((CraftBlock) block).getPosition();
    }

    @Override
    public IChatBaseComponent G_() {
        return this.a;
    }

    @Override
    public Container createMenu(int var1, PlayerInventory var2, EntityHuman var3) {
        return new ContainerAnvilLot(var1, var2, ContainerAccess.a(var3.Y(), pos));
    }
}
