package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.client.GuiContainerEZ;
import com.zerofall.ezstorage.gui.client.GuiStorageCore;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiStorageCore.class)
public abstract class MixinGuiStorageCore extends GuiContainerEZ {

    public MixinGuiStorageCore(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Invoker(value = "searchBoxChange", remap = false)
    protected abstract void invokeSearchBoxChange(String text);

    @Inject(method = "drawScreen", at = @At(value = "TAIL"))
    private void injectDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;setText(Ljava/lang/String;)V"))
    private void redirectMouseClickedSetText(GuiTextField searchField, String textIn) {
        this.invokeSearchBoxChange(textIn);
    }
}
