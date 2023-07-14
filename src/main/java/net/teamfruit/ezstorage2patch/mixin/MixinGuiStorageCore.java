package net.teamfruit.ezstorage2patch.mixin;

import com.zerofall.ezstorage.gui.client.GuiContainerEZ;
import com.zerofall.ezstorage.gui.client.GuiStorageCore;
import com.zerofall.ezstorage.util.ItemGroup;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.teamfruit.ezstorage2patch.imixin.IGuiStorageCore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiStorageCore.class)
public abstract class MixinGuiStorageCore extends GuiContainerEZ implements IGuiStorageCore {

    @Shadow(remap = false)
    private List<ItemGroup> filteredList;

    @Shadow(remap = false)
    private GuiTextField searchField;

    public MixinGuiStorageCore(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public List<ItemGroup> getFilteredList() {
        return this.filteredList;
    }

    @Override
    @Invoker(value = "getSlotAt", remap = false)
    public abstract Integer invokeGetSlotAt(int x, int y);

    @Invoker(value = "searchBoxChange", remap = false)
    public abstract void invokeSearchBoxChange(String text);

    @Inject(method = "drawScreen", at = @At(value = "TAIL"))
    private void injectDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Redirect(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;setText(Ljava/lang/String;)V"))
    private void redirectMouseClickedSetText(GuiTextField searchField, String textIn) {
        this.invokeSearchBoxChange(textIn);
    }

    @Inject(method = "mouseClicked", at = @At(value = "JUMP", ordinal = 5, shift = At.Shift.BEFORE))
    private void injectMouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        this.searchField.setFocused(false);
    }
}
