package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.teamfruit.ezstorage2patch.integration.gregtech.GTUtil;
import net.teamfruit.ezstorage2patch.imixin.IEZInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(EZInventory.class)
public abstract class MixinEZInventory implements IEZInventory {

    @Shadow(remap = false)
    private List<ItemGroup> inventory;

    @Invoker(value = "extractStack", remap = false)
    protected abstract ItemStack invokeExtractStack(ItemGroup group, int size, boolean peek);

    @Inject(method = "getItemsAt(IIIZ)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lcom/zerofall/ezstorage/util/EZInventory;extractStack(Lcom/zerofall/ezstorage/util/ItemGroup;IZ)Lnet/minecraft/item/ItemStack;", ordinal = 0), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void injectGetItemsAtExtractStack(int index, int type, int size, boolean peek, CallbackInfoReturnable<ItemStack> cir, ItemGroup group, ItemStack stack) {
        if (size == 0) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (size < 1) {
            if (type == 1) {
                size = (((int) Math.min(stack.getMaxStackSize(), group.count)) + 2 - 1) / 2;
            } else if (type == 2) {
                size = 1;
            }
        }

        ItemStack result = this.invokeExtractStack(group, size, peek);
        cir.setReturnValue(result);
    }

    @Override
    public ItemStack getItemWithoutExtractAt(int index) {
        if (index >= this.inventory.size()) {
            return ItemStack.EMPTY;
        }
        return this.inventory.get(index).itemStack;
    }

    @Override
    public ItemStack input(ItemStack itemStack, int quantity, boolean sort) {
        int stackCount = itemStack.getCount();
        quantity = Math.min(itemStack.getCount(), quantity);

        ItemStack inputStack = itemStack.copy();
        inputStack.setCount(Math.min(stackCount, quantity));
        ItemStack inputResult = ((EZInventory) (Object) this).input(inputStack, sort);
        if (inputResult.isEmpty()) {
            itemStack.shrink(quantity);
        } else {
            itemStack.setCount(stackCount - quantity + inputResult.getCount());
        }
        return itemStack;
    }

    @Override
    public ItemStack getItemsForRecipeSync(ItemStack[] itemStacks) {
        ItemStack result = ((EZInventory) (Object) this).getItems(itemStacks);
        if (!result.isEmpty()) {
            return result;
        }

        for (ItemStack searchItemStack : itemStacks) {
            Item searchItem = searchItemStack.getItem();
            ResourceLocation searchResourceLocation = searchItem.getRegistryName();
            if (searchResourceLocation == null || !searchResourceLocation.getNamespace().equals("gregtech")) {
                continue;
            }

            for (ItemGroup invItemGroup : this.inventory) {
                int searchItemCount = searchItemStack.getCount();
                if (GTUtil.stackEqualGT(searchItemStack, invItemGroup.itemStack) && searchItemCount <= invItemGroup.count) {
                    ItemStack retrieved = invItemGroup.itemStack.copy();
                    retrieved.setCount(searchItemCount);
                    invItemGroup.count -= searchItemCount;
                    if (invItemGroup.count <= 0) {
                        this.inventory.remove(invItemGroup);
                    }
                    return retrieved;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
