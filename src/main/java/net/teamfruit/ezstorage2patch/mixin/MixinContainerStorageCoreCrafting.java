package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.server.ContainerStorageCore;
import com.zerofall.ezstorage.gui.server.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.teamfruit.ezstorage2patch.imixin.IEZInventory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ContainerStorageCoreCrafting.class)
public abstract class MixinContainerStorageCoreCrafting extends ContainerStorageCore {

    @Shadow(remap = false)
    private InventoryCrafting craftMatrix;

    @Shadow(remap = false)
    private IInventory craftResult;

    private boolean craftMatrixChanged = true;
    private ItemStack craftResultCache;

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

    @Inject(method = "transferStackInSlot", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;", ordinal = 1, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectTransferStackInSlot1(EntityPlayer playerIn, int index, CallbackInfoReturnable<ItemStack> cir, Slot slotObject, ItemStack[] recipe, ItemStack itemstack1, ItemStack itemstack) {
        this.craftMatrixChanged = false;
        this.craftResultCache = itemstack;
    }

    @Inject(method = "transferStackInSlot", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/gui/server/ContainerStorageCoreCrafting;tryToPopulateCraftingGrid([Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;)V", shift = At.Shift.AFTER, remap = false), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void injectTransferStackInSlot2(EntityPlayer playerIn, int index, CallbackInfoReturnable<ItemStack> cir, Slot slotObject) {
        if (this.craftMatrixChanged) {
            this.onCraftMatrixChanged(this.craftMatrix);
            this.craftResultCache = ItemStack.EMPTY;
            cir.setReturnValue(this.craftResultCache);
        } else {
            slotObject.putStack(this.craftResultCache);
        }
    }

    @Inject(method = "transferStackInSlot", at = @At(value = "RETURN"))
    private void injectTransferStackInSlot3(EntityPlayer playerIn, int index, CallbackInfoReturnable<ItemStack> cir) {
        this.craftResultCache = null;
        this.craftMatrixChanged = true;
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
        if (!slot1.getHasStack()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        int heldItemCountBeforeCraft = player.inventory.getItemStack().getCount();
        ItemStack resultBeforeCraft = slot1.getStack().copy();
        this.craftMatrixChanged = false;
        ItemStack result = super.slotClick(slotId, dragType, clickTypeIn, player);
        int heldItemCountAfterCraft = player.inventory.getItemStack().getCount();
        if (clickTypeIn == ClickType.PICKUP && heldItemCountBeforeCraft < heldItemCountAfterCraft) {
            this.invokeTryToPopulateCraftingGrid(recipe, player);
            if (this.craftMatrixChanged) {
                this.onCraftMatrixChanged(this.craftMatrix);
            } else {
                slot1.putStack(resultBeforeCraft);
            }
        }
        this.craftMatrixChanged = true;
        cir.setReturnValue(result);
    }

    @Redirect(method = "tryToPopulateCraftingGrid", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/gui/server/ContainerStorageCoreCrafting;getSlotFromInventory(Lnet/minecraft/inventory/IInventory;I)Lnet/minecraft/inventory/Slot;"), remap = false)
    private Slot redirectTryToPopulateCraftingGridGetSlotFromInventory(ContainerStorageCoreCrafting containerStorageCoreCrafting, IInventory inventory, int slotIn) {
        Slot slot = containerStorageCoreCrafting.getSlotFromInventory(inventory, slotIn);
        if (slot != null && slot.getHasStack()) {
            return null;
        }
        return slot;
    }

    @Inject(method = "tryToPopulateCraftingGrid", at = @At(value = "JUMP", ordinal = 5), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectTryToPopulateCraftingGrid2(ItemStack[] recipe, EntityPlayer playerIn, CallbackInfo ci, int j, Slot slot, ItemStack retrieved) {
        if (retrieved.isEmpty()) {
            this.craftMatrixChanged = true;
        }
    }

    @Redirect(method = "tryToPopulateCraftingGrid", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/util/EZInventory;getItems([Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"), remap = false)
    private ItemStack redirectTryToPopulateCraftingGridGetItems(EZInventory ezInventory, ItemStack[] itemStacks) {
        return ((IEZInventory) (Object) ezInventory).getItemsForRecipeSync(itemStacks);
    }

    @Inject(method = "onCraftMatrixChanged", at = @At(value = "HEAD"), cancellable = true)
    private void injectOnCraftMatrixChanged(IInventory inventoryIn, CallbackInfo ci) {
        if (!this.craftMatrixChanged) {
            ci.cancel();
        }
    }

    @Override
    public boolean canMergeSlot(@NotNull ItemStack stack, Slot slotIn) {
        return !slotIn.inventory.equals(this.craftResult) && super.canMergeSlot(stack, slotIn);
    }
}
