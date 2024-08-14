package dev.qruet.anvillot.nms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import dev.qruet.anvillot.util.ReflectionUtils;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.ITileInventory;

public class VersionHandlerNMS extends VersionHandler {

    private static String VERSION;
    public static final int MINECRAFT_BASE_VERSION;

    private static final Class<?> CraftPlayer;
    private static final Method getHandle;
    private static final Class<?> IChatMutableComponent;
    private static final Class<?> ComponentContentsClass = ReflectionUtils.getNMSClassByFullName("network.chat.ComponentContents");
    private static final Class<?> TranslatableContentsClass = ReflectionUtils.getNMSClassByFullName("network.chat.contents.TranslatableContents");
    private static Constructor<?> TranslatableContentsConstructor;

    private static Method openContainer117;
    private static Class<?> AnvilLotTileInventory;

    static {
        VERSION = ReflectionUtils.getVersion();
        MINECRAFT_BASE_VERSION = Integer.parseInt(VERSION.substring(3, VERSION.indexOf('_', 3)));

        CraftPlayer = ReflectionUtils.getCraftBukkitClass("entity.CraftPlayer");

        try {
            AnvilLotTileInventory = getLocalNMSClass("AnvilLotTileInventory");
        } catch (ClassNotFoundException ignored) {
        }

        IChatMutableComponent = ReflectionUtils.getNMSClassByFullName("network.chat.IChatMutableComponent");
        try {TranslatableContentsConstructor = TranslatableContentsClass.getDeclaredConstructor(String.class, String.class, Object[].class);
        } catch (NoSuchMethodException ignored) {
        }

        getHandle = ReflectionUtils.getMethod(CraftPlayer, "getHandle");

        if (MINECRAFT_BASE_VERSION <= 17) {
            openContainer117 = ReflectionUtils.getMethod(EntityPlayer.class, "openContainer", ITileInventory.class);
        }
    }

    public static Class<?> getLocalNMSClass(String className) throws ClassNotFoundException {
        return Class.forName(VersionHandlerNMS.class.getPackage().getName() + "." + VERSION + "." + className);
    }

    @Deprecated
    public void openContainer(Player player) {
        this.openContainer(player, player.getTargetBlock(new HashSet<Material>() {{
            add(Material.ANVIL);
        }}, 5));
    }

    public void openContainer(Player player, Block block) {
        if (AnvilLotTileInventory == null)
            throw new UnsupportedOperationException("VersionHandler has not yet been initialized.");
        try {
            EntityPlayer eplayer = (EntityPlayer) getHandle.invoke(CraftPlayer.cast(player));
            IChatBaseComponent title;
            if (MINECRAFT_BASE_VERSION > 18) {
                title = (IChatBaseComponent) IChatMutableComponent.getMethod("a", ComponentContentsClass).invoke(null, TranslatableContentsConstructor.newInstance("container.repair", "Repair & Name", new Object[]{}));
            } else {
                title = new ChatMessage("container.repair");
            }
            ITileInventory tileInventory = (ITileInventory) AnvilLotTileInventory.getConstructor(Block.class, IChatBaseComponent.class).newInstance(block, title);
            if (MINECRAFT_BASE_VERSION > 17) {
                eplayer.a(tileInventory);
            } else {
                openContainer117.invoke(eplayer, tileInventory);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }


}
