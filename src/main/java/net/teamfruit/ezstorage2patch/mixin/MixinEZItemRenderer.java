package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.util.EZItemRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EZItemRenderer.class)
public abstract class MixinEZItemRenderer extends RenderItem {

    public MixinEZItemRenderer(TextureManager p_i46552_1_, ModelManager p_i46552_2_, ItemColors p_i46552_3_) {
        super(p_i46552_1_, p_i46552_2_, p_i46552_3_);
    }

    @Inject(method = "renderItemOverlayIntoGUI", at = @At(value = "JUMP", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void injectRenderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String amtString, CallbackInfo ci) {
        ItemStack fakeItem = stack.copy();
        fakeItem.setCount(1);
        super.renderItemOverlayIntoGUI(fr, fakeItem, xPosition, yPosition, null);
    }

    @Redirect(method = "renderItemOverlayIntoGUI", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;showDurabilityBar(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean redirectRenderItemOverlayIntoGUIShowDurabilityBar(Item item, ItemStack stack) {
        return false;
    }
}
