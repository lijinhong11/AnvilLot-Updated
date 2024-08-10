package dev.qruet.anvillot.nms.v1_21_R1;

import dev.qruet.anvillot.bar.v1_21_R1.ExperienceBar;
import dev.qruet.anvillot.bar.v1_21_R1.HardLimitBar;
import dev.qruet.anvillot.bar.v1_21_R1.TooExpensiveBar;
import dev.qruet.anvillot.config.GeneralPresets;
import dev.qruet.anvillot.config.assets.SoundMeta;
import dev.qruet.anvillot.nms.IContainerAnvilLot;
import dev.qruet.anvillot.util.L;
import dev.qruet.anvillot.util.java.LiveReflector;
import dev.qruet.anvillot.util.num.Int;
import net.minecraft.network.protocol.game.PacketPlayOutGameStateChange;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.tags.TagsBlock;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.InventoryClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BlockAnvil;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarFlag;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lijinhong11
 * @version 3.6.0
 */
public class ContainerAnvilLot extends ContainerAnvil implements IContainerAnvilLot {

    private LiveReflector<Integer> u;

    private final EntityPlayer owner;

    private ExperienceBar expBar;
    private TooExpensiveBar errBar;
    private HardLimitBar hlmBar;

    private int maxCost = -1;
    private int repairCost;

    private final PacketPlayOutGameStateChange defaultMode;

    public ContainerAnvilLot(int i, PlayerInventory playerinventory, final ContainerAccess containeraccess) {
        super(i, playerinventory, containeraccess);

        try {
            Field u = ContainerAnvil.class.getDeclaredField("u");
            this.u = new LiveReflector<>(this, u);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        super.maximumRepairCost = Integer.MAX_VALUE;

        owner = ((CraftPlayer) playerinventory.getOwner()).getHandle();

        if (GeneralPresets.EXPERIENCE_BAR_ENABLED) {
            List<BarFlag> expFlagList = new ArrayList<>();
            if (GeneralPresets.ExperienceBarPresets.FOG)
                expFlagList.add(BarFlag.CREATE_FOG);
            if (GeneralPresets.ExperienceBarPresets.DARK_SKY)
                expFlagList.add(BarFlag.DARKEN_SKY);

            expBar = new ExperienceBar(getOwner(), expFlagList.toArray(new BarFlag[0]));
            expBar.enable();
        }

        if (GeneralPresets.TOO_EXPENSIVE_BAR_ENABLED) {
            List<BarFlag> errFlagList = new ArrayList<>();
            if (GeneralPresets.TooExpensiveBarPresets.FOG)
                errFlagList.add(BarFlag.CREATE_FOG);
            if (GeneralPresets.TooExpensiveBarPresets.DARK_SKY)
                errFlagList.add(BarFlag.DARKEN_SKY);
            errBar = new TooExpensiveBar(this, errFlagList.toArray(new BarFlag[0]));
        }

        if (GeneralPresets.HARD_LIMIT_BAR_ENABLED) {
            List<BarFlag> errFlagList = new ArrayList<>();
            if (GeneralPresets.HardLimitBarPresets.FOG)
                errFlagList.add(BarFlag.CREATE_FOG);
            if (GeneralPresets.HardLimitBarPresets.DARK_SKY)
                errFlagList.add(BarFlag.DARKEN_SKY);
            hlmBar = new HardLimitBar(this, errFlagList.toArray(new BarFlag[0]));
        }

        maxCost = GeneralPresets.DEFAULT_MAX_COST;

        getOwner().getEffectivePermissions().forEach(pI -> {
            String permission = pI.getPermission();
            if (!permission.startsWith("anvillot.limit."))
                return;
            this.maxCost = Int.P(permission.substring("anvillot.limit.".length()));
        });

        L.R(new Listener() {
            @EventHandler
            public void onDamage(EntityDamageEvent e) {
                if (!(e.getEntity() instanceof Player))
                    return;
                Player player = (Player) e.getEntity();
                if (player.getUniqueId().equals(getOwner().getUniqueId())) {
                    player.closeInventory();
                    HandlerList.unregisterAll(this);
                }
            }
        });

        maxCost = maxCost == -1 ? Integer.MAX_VALUE : maxCost;

        defaultMode = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 1);

        owner.w();

        this.repairCost = w.b();
    }

    @Override
    public void a(EntityHuman entityhuman, ItemStack itemstack) {
        if (!entityhuman.fZ().d) {
            entityhuman.c(-1 * repairCost);
            if (expBar != null)
                expBar.update();
        }

        //Container
        sendSlotUpdate(-1, new ItemStackWrapper(entityhuman.fY().l.cc.g()), -1, k());

        q.a(0, ItemStack.l);
        if (u.get() > 0) {
            ItemStack itemstack1 = q.a(1);
            //isEmpty && getPopTime
            if (!itemstack1.e() && itemstack1.H() > u.get()) {
                itemstack1.g(u.get());
                q.a(1, itemstack1);
            } else {
                q.a(1, ItemStack.l);
            }
        } else {
            q.a(1, ItemStack.l);
        }

        updateRepairCost(0);

        this.o.a((world, blockposition) -> {
            IBlockData iblockdata = world.a_(blockposition);
            //getAbilities() && getRandom().nextFloat()
            if (!entityhuman.fZ().d && iblockdata.a(TagsBlock.G) && entityhuman.dR().i() < 0.12F) {
                IBlockData iblockdata1 = BlockAnvil.e(iblockdata);
                if (iblockdata1 == null) {
                    world.a(blockposition, false);
                    world.c(1029, blockposition, 0);
                    //getInventory().f
                    entityhuman.fY().f(itemstack);
                } else {
                    world.a(blockposition, iblockdata1, 2);
                    world.c(1030, blockposition, 0);
                }
            } else {
                world.c(1030, blockposition, 0);
            }

        });
    }

    @Override
    public void transferTo(Container other, CraftHumanEntity player) {
        if (expBar != null)
            expBar.destroy();
        if (errBar != null)
            errBar.destroy();
        if (hlmBar != null)
            hlmBar.destroy();
        reset();
    }

    @Override
    public boolean b(EntityHuman entityhuman) {
        return !this.checkReachable || this.o.<Boolean>a((world, blockposition) -> this.a(world.a_(blockposition)) && entityhuman.i((double) blockposition.u() + 0.5D, (double) blockposition.v() + 0.5D, (double) blockposition.w() + 0.5D) <= 64.0D, true);
    }

    @Override
    public void a(int i, int j, InventoryClickType inventoryclicktype, EntityHuman entityhuman) {
        if (i == 2 && ((hlmBar != null && hlmBar.isEnabled()) || (errBar != null && errBar.isEnabled()))) {
            SoundMeta sM = GeneralPresets.DISABLED_ALERT;
            if (sM != null) {
                getOwner().playSound(
                        getOwner().getLocation(),
                        sM.getSound(),
                        sM.getVolume(),
                        sM.getPitch());
            }
        } else {
            super.a(i, j, inventoryclicktype, entityhuman);
        }
    }

    @Override
    public void i() {
        super.i();

        ItemStack first = q.a(0); // retrieve item from index 0 in repair inventory
        ItemStack second = q.a(1); // retrieve item from index 1 in repair inventory
        ItemStack result = r.a(0); // retrieve item from index 0 in result inventory

        PrepareAnvilEvent event = new PrepareAnvilEvent(getBukkitView(),
                CraftItemStack.asCraftMirror(result).clone());
        Bukkit.getPluginManager().callEvent(event);

        result = CraftItemStack.asNMSCopy(event.getResult());
        q.a(0, result);

        owner.c.a(defaultMode);

        IContainerAnvilLot.super.calculate(
                new ItemStackWrapper(first),
                new ItemStackWrapper(second),
                new ItemStackWrapper(result),
                w.b());

        sendSlotUpdate(2, new ItemStackWrapper(r.a(0)), j, k());
    }

    public void updateRepairCost(int val) {

        repairCost = val;
        w.a(val);

        if (expBar != null)
            expBar.update();

        if (repairCost == -1 && GeneralPresets.HARD_LIMIT) {
            PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 3);
            owner.c.a(packet);

            if (hlmBar != null) {
                if (!hlmBar.isEnabled())
                    hlmBar.enable();
            }

            sendSlotUpdate(2, new ItemStackWrapper(r.a(0)), j, k());

            return;
        }

        if (getOwner().getLevel() < repairCost) {
            PacketPlayOutGameStateChange packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, 3);
            owner.c.a(packet);

            w.a(40);

            if (errBar != null) {
                if (!errBar.isEnabled())
                    errBar.enable();
                else
                    errBar.update();
            }

            sendSlotUpdate(2, new ItemStackWrapper(r.a(0)), j, k());

            return;
        }

        if (hlmBar != null && hlmBar.isEnabled())
            hlmBar.disable();

        if (errBar != null && errBar.isEnabled())
            errBar.disable();

        super.d();

    }

    @Override
    public boolean a(String s) {
        boolean b = super.a(s);

        if (CraftItemStack.asBukkitCopy(q.a(0)).getType().isAir())
            return false;

        if (!q.a(0).w().getString().equals(s)) {
            b = true;
            updateRepairCost(repairCost + 1);
        } else if (q.a(0).w().getString().equals(s)) {
            b = false;
            updateRepairCost(repairCost - 1);
        }

        return b;
    }

    public int getCost() {
        return repairCost;
    }

    public Player getOwner() {
        return owner.getBukkitEntity();
    }

    @Override
    public int getMaximumCost() {
        return maxCost;
    }

    @Override
    public String getRenameText() {
        return v;
    }

    private void reset() {
        owner.c.a(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.d, owner.e.b().a()));
        owner.w();
    }
}
