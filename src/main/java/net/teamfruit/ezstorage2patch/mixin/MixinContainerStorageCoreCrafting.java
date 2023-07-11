package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.server.ContainerStorageCore;
import com.zerofall.ezstorage.gui.server.ContainerStorageCoreCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ContainerStorageCoreCrafting.class)
public abstract class MixinContainerStorageCoreCrafting extends ContainerStorageCore {

    @Shadow(remap = false)
    private InventoryCrafting craftMatrix;

    public MixinContainerStorageCoreCrafting(EntityPlayer player, World world, int x, int y, int z) {
        super(player, world, x, y, z);
    }

    @Redirect(method = "transferStackInSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEqual(Lnet/minecraft/item/ItemStack;)Z", ordinal = 0))
    private boolean redirectTransferStackInSlotIsItemEqual0(ItemStack stack, ItemStack other) {
        return true;
    }

    @Redirect(method = "transferStackInSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEqual(Lnet/minecraft/item/ItemStack;)Z", ordinal = 1))
    private boolean redirectTransferStackInSlotIsItemEqual1(ItemStack stack, ItemStack other) {
        return false;
    }

    @Redirect(method = "transferStackInSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryCrafting;getStackInSlot(I)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack redirectTransferStackInSlotGetStackInSlot(InventoryCrafting inv, int index) {
        return inv.getStackInSlot(index).copy();
    }

    @ModifyConstant(method = "tryToPopulateCraftingGrid", constant = @Constant(intValue = 1), slice = @Slice(from = @At("HEAD"), to = @At(value = "CONSTANT", args = "intValue=1", ordinal = 0)), remap = false)
    private int modifyConstantTryToPopulateCraftingGrid(int original) {
        return Integer.MAX_VALUE;
    }

    @Redirect(method = "tryToPopulateCraftingGrid", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setCount(I)V"))
    private void redirectTryToPopulateCraftingGridSetCount(ItemStack itemStack, int count) {
        if (itemStack.getCount() > 1) {
            itemStack.setCount(itemStack.getCount() - 1);
        }
    }

    @Redirect(method = "tryToPopulateCraftingGrid", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/gui/server/ContainerStorageCoreCrafting;clearGrid(Lnet/minecraft/entity/player/EntityPlayer;)V", remap = false), remap = false)
    private void redirectTryToPopulateCraftingGridClearGrid(ContainerStorageCoreCrafting container, EntityPlayer player) {
        clearGridWithoutContainerItem(player);
    }

    private void clearGridWithoutContainerItem(EntityPlayer player) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = this.craftMatrix.getStackInSlot(i);
            if (!stack.isEmpty() && !stack.getItem().hasContainerItem(stack)) {
                ItemStack result = this.tileEntity.input(stack);
                this.craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
                if (!result.isEmpty()) {
                    player.dropItem(result, false);
                }
            }
        }
    }

    @Invoker(value = "tryToPopulateCraftingGrid", remap = false)
    protected abstract void invokeTryToPopulateCraftingGrid(ItemStack[] recipe, EntityPlayer playerIn);

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/gui/server/ContainerStorageCore;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void injectSlotClick1(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir, Slot slot1, ItemStack[] recipe) {
        int heldItemCountBeforeCraft = player.inventory.getItemStack().getCount();
        ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
        int heldItemCountAfterCraft = player.inventory.getItemStack().getCount();
        if (heldItemCountBeforeCraft < heldItemCountAfterCraft) {
            this.invokeTryToPopulateCraftingGrid(recipe, player);
        }
        cir.setReturnValue(result);
    }

}
