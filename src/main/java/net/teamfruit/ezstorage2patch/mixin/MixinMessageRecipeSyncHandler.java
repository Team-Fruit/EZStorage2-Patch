package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.network.MessageRecipeSync;
import com.zerofall.ezstorage.util.EZInventory;
import net.minecraft.item.ItemStack;
import net.teamfruit.ezstorage2patch.IEZInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MessageRecipeSync.Handler.class)
public abstract class MixinMessageRecipeSyncHandler {

    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/util/EZInventory;getItems([Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", remap = false), remap = false)
    private ItemStack redirectHandleGetItems(EZInventory inventory, ItemStack[] itemStacks) {
        return ((IEZInventory) (Object) inventory).getItemsForRecipeSync(itemStacks);
    }

}
