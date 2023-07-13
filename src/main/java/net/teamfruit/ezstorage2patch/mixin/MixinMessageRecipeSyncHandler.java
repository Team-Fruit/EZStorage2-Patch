package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.server.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.network.MessageRecipeSync;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.teamfruit.ezstorage2patch.integration.gregtech.GTUtil;
import net.teamfruit.ezstorage2patch.imixin.IEZInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MessageRecipeSync.Handler.class)
public abstract class MixinMessageRecipeSyncHandler {

    @Shadow(remap = false)
    private ItemStack[][] recipe;

    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/util/EZInventory;getItems([Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", remap = false), remap = false)
    private ItemStack redirectHandleGetItems(EZInventory inventory, ItemStack[] itemStacks) {
        return ((IEZInventory) (Object) inventory).getItemsForRecipeSync(itemStacks);
    }

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectHandle(EntityPlayerMP player, MessageRecipeSync message, CallbackInfo ci, Container container, ContainerStorageCoreCrafting con, TileEntityStorageCore tileEntity, int x, Slot slot, ItemStack retreived) {
        if (!retreived.isEmpty()) {
            return;
        }

        for (ItemStack searchItemStack : this.recipe[x]) {
            for (ItemStack invItemStack : player.inventory.mainInventory) {
                if (EZInventory.stacksEqualOreDict(searchItemStack, invItemStack) || GTUtil.stackEqualGT(searchItemStack, invItemStack)) {
                    player.inventory.deleteStack(invItemStack);
                    slot.putStack(invItemStack);
                    return;
                }
            }
        }
    }

}
