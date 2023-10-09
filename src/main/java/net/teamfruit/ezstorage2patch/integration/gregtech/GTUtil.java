package net.teamfruit.ezstorage2patch.integration.gregtech;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GTUtil {

    private static Class<?> iGTTool;
    private static Method iGTToolGetDomain;
    private static Method iGTToolGetId;

    static {
        try {
            iGTTool = Class.forName("gregtech.api.items.toolitem.IGTTool", false, Launch.classLoader);
            iGTToolGetDomain = iGTTool.getMethod("getDomain");
            iGTToolGetId = iGTTool.getMethod("getId");
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
    }

    public static boolean stackEqualGT(ItemStack stack1, ItemStack stack2) {
        if (equalsResourceLocation(stack1, stack2)) {
            return true;
        }
        return equalsGTTool(stack1, stack2);
    }

    private static boolean equalsResourceLocation(ItemStack stack1, ItemStack stack2) {
        Item item1 = stack1.getItem();
        Item item2 = stack2.getItem();
        ResourceLocation resourceLocation1 = item1.getRegistryName();
        ResourceLocation resourceLocation2 = item2.getRegistryName();

        if (resourceLocation1 == null || resourceLocation2 == null) {
            return false;
        }
        if (!resourceLocation1.getNamespace().equals("gregtech") || !resourceLocation2.getNamespace().equals("gregtech")) {
            return false;
        }
        if (!resourceLocation1.getPath().equals(resourceLocation2.getPath())) {
            return false;
        }
        return stack1.getMetadata() == stack2.getMetadata();
    }

    private static boolean equalsGTTool(ItemStack stack1, ItemStack stack2) {
        if (iGTTool == null || iGTToolGetDomain == null || iGTToolGetId == null) {
            return false;
        }
        if (!iGTTool.isInstance(stack1.getItem()) || !iGTTool.isInstance(stack2.getItem())) {
            return false;
        }
        try {
            if(!iGTToolGetDomain.invoke(stack1.getItem()).equals(iGTToolGetDomain.invoke(stack2.getItem()))) {
                return false;
            }
            return iGTToolGetId.invoke(stack1.getItem()).equals(iGTToolGetId.invoke(stack2.getItem()));
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

}
