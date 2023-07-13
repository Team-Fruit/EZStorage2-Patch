package net.teamfruit.ezstorage2patch.integration.gregtech;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GTUtil {

    public static boolean stackEqualGT(ItemStack stack1, ItemStack stack2) {
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
}
