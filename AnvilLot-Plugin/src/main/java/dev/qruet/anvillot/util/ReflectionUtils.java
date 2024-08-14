package dev.qruet.anvillot.util;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReflectionUtils {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();
    private static final boolean isPaper = isPaper();

    /**
     * Legacy (versions pre 1.17)
     *
     * @return Legacy version support enabled
     */
    public static boolean isLegacy() {
        return ReflectionUtils.getIntVersion() < 1170;
    }

    private static Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.getOrDefault(clazz, clazz);
    }

    private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++) {
            types[i] = getPrimitiveType(classes[i]);
        }
        return types;
    }


    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
                return false;
        return true;
    }

    /**
     * @param method Method to be invoked
     * @param obj    Instance of class where method exists
     * @return Returns any objects that the method may return
     */
    public static Object invokeMethod(String method, Object obj) {
        try {
            return getMethod(method, obj.getClass()).invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Calls a method
     *
     * @param method Method to be invoked
     * @param obj    Instance of the class where method exists
     * @param args   Arguments in the method to be passed
     * @return Returns any objects that the method may return
     */
    public static Object invokeMethodWithArgs(String method, Object obj, Object... args) {
        try {
            Class<?>[] classes = new Class<?>[args.length];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = args[i].getClass();
            }
            return getMethod(method, obj.getClass(), classes).invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves a method instance
     *
     * @param name       Name of method
     * @param clazz      Class where method exists
     * @param paramTypes Parameters the class may have
     * @return Instance of method
     */
    public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t))
                return m;
        }
        return null;
    }

    /**
     * Get version of server
     *
     * @return String version
     */
    public static String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        if (isPaper && getIntRawVersion() >= 1205) {
            switch (getIntRawVersion()) {
                case 1205:
                case 1206:
                    return "v1_20_R4";
                case 1210:
                case 1211:
                    return "v1_21_R1";
            }
        }

        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static int getIntVersion() {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(getVersion());
        StringBuilder strInt = new StringBuilder();
        while (m.find()) {
            strInt.append(m.group());
        }

        String str = strInt.toString();
        if (str.isEmpty()) {
            return getIntRawVersion();
        }

        int version = -1;
        try {
            version = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * Converts string name of class to Class object
     *
     * @param className Name of class
     * @return Class object
     * @deprecated Legacy NMS support
     */
    public static Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + "." + className;

        if (isPaper && getIntRawVersion() >= 1205) {
            fullName = "net.minecraft.server." + className;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    public static Class<?> getNMSClassByFullName(String className) {
        try {
            return Class.forName("net.minecraft." + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Use this instead of getNMSClass for classes in the craftbukkit package
     *
     * @param className Name of class
     * @return Class Object
     */
    public static Class<?> getCraftBukkitClass(String className) {
        String fullName = "org.bukkit.craftbukkit." + getVersion() + "." + className;

        if (isPaper() && getIntRawVersion() >= 1205) {
            fullName = "org.bukkit.craftbukkit." + className;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Retrieve fields from classes
     *
     * @param clazz Class where field exists
     * @param name  Name of field
     * @return Field object
     */
    public static Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getField(Object instance, String name) {
        if (instance == null)
            return null;

        Field field = getField(instance.getClass(), name);
        if (field == null)
            return null;

        Object val = null;
        try {
            val = field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return val;
    }

    /**
     * Retrieve method instance
     *
     * @param clazz Name of class where method exists
     * @param name  Name of method
     * @param args  Parameters that method may have
     * @return Method instance
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getDeclaredMethods())
            if (m.getName().equals(name)
                    && (args.length == 0 || ClassListEqual(args,
                    m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        return null;
    }

    private static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length)
            return false;
        for (int i = 0; i < l1.length; i++)
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        return equal;
    }

    private static boolean isPaper() {
        boolean isPaper = false;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            isPaper = true;
        } catch (ClassNotFoundException e) {
        }
        return isPaper;
    }

    private static int getIntRawVersion() {
        String mcVersion = Bukkit.getBukkitVersion().split("-")[0];
        String rawIntStr = mcVersion.replaceAll("\\.", "");
        if (rawIntStr.length() < 4) {
            rawIntStr = rawIntStr + "0";
        }
        return Integer.parseInt(rawIntStr);
    }
}
