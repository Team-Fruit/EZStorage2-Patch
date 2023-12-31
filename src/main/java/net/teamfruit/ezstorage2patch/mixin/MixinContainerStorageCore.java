package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.server.ContainerStorageCore;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.teamfruit.ezstorage2patch.imixin.IEZInventory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ContainerStorageCore.class)
public abstract class MixinContainerStorageCore extends Container {

    @Shadow(remap = false)
    private TileEntityStorageCore tileEntity;

    @Invoker(value = "rowCount", remap = false)
    protected abstract int invokeRowCount();

    private IInventory inventory;

    @Inject(method = "<init>", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectConstructor(EntityPlayer player, World world, int x, int y, int z, CallbackInfo ci, int startingY, int startingX, IInventory inventory) {
        this.inventory = inventory;
    }

    @Inject(method = "slotClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Container;slotClick(IILnet/minecraft/inventory/ClickType;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void injectSlotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir, ItemStack val) {
        if (slotId >= 0) {
            Slot slot = this.getSlot(slotId);
            if (!(slot instanceof SlotCrafting) && clickTypeIn == ClickType.QUICK_MOVE && slot.canTakeStack(player)) {
                ItemStack itemStack = slot.getStack();
                ItemStack result = this.tileEntity.inventory.input(itemStack, true);
                slot.onSlotChanged();
                super.detectAndSendChanges();
                cir.setReturnValue(result.copy());
            }
        }
    }

    @Inject(method = "customSlotClick", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void injectCustomSlotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> cir) {
        // Always return EMPTY since this return value is never used
        cir.setReturnValue(ItemStack.EMPTY);

        int type = 0;
        if (clickedButton == 1) {
            type = (mode == 0) ? 1 : 2;
        }

        // isShiftLeftClick
        if (clickedButton == 0 && mode == 1) {
            int playerInventoryStartIndex = this.invokeRowCount() * 9;
            int playerInventoryEndIndex = playerInventoryStartIndex + playerIn.inventory.mainInventory.size();

            if (playerIn.inventory.getFirstEmptyStack() < 0) {
                ItemStack targetStack = ((IEZInventory) (Object) this.tileEntity.inventory).getItemWithoutExtractAt(slotId);
                int emptyCapacity = this.inventorySlots.subList(playerInventoryStartIndex, playerInventoryEndIndex).stream().mapToInt(slot -> {
                    ItemStack slotStack = slot.getStack();
                    if (slotStack.isItemEqual(targetStack) && ItemStack.areItemStackTagsEqual(slotStack, targetStack)) {
                        return slotStack.getMaxStackSize() - slotStack.getCount();
                    }
                    return 0;
                }).sum();

                ItemStack retrievedStack = this.tileEntity.inventory.getItemsAt(slotId, type, Math.min(emptyCapacity, targetStack.getMaxStackSize()));
                if (!retrievedStack.isEmpty()) {
                    this.mergeItemStack(retrievedStack, playerInventoryStartIndex, playerInventoryEndIndex, true);
                }
            } else {
                ItemStack retrievedStack = this.tileEntity.inventory.getItemsAt(slotId, type);
                if (!retrievedStack.isEmpty()) {
                    this.mergeItemStack(retrievedStack, playerInventoryStartIndex, playerInventoryEndIndex, true);
                }
            }
        } else {
            ItemStack heldStack = playerIn.inventory.getItemStack();
            if (heldStack.isEmpty()) {
                ItemStack retrievedStack = this.tileEntity.inventory.getItemsAt(slotId, type);
                playerIn.inventory.setItemStack(retrievedStack);
            } else if (clickedButton == 0) {
                playerIn.inventory.setItemStack(this.tileEntity.inventory.input(heldStack));
            } else if (clickedButton == 1 && mode != 1) {
                playerIn.inventory.setItemStack(((IEZInventory) (Object) this.tileEntity.inventory).input(heldStack, 1));
            }
        }
    }

    @Override
    public boolean canDragIntoSlot(@NotNull Slot slotIn) {
        return !slotIn.inventory.equals(this.inventory);
    }
}
