package net.teamfruit.ezstorage2patch.imixin;

import net.minecraft.item.ItemStack;

public interface IEZInventory {

    ItemStack getItemWithoutExtractAt(int index);

    ItemStack input(ItemStack itemStack, int quantity, boolean sort);

    default ItemStack input(ItemStack itemStack, int quantity) {
        return this.input(itemStack, quantity, true);
    }

    ItemStack getItemsForRecipeSync(ItemStack[] itemStacks);
}
