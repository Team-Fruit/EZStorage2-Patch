package net.teamfruit.ezstorage2patch.integration.jei;

import com.zerofall.ezstorage.gui.client.GuiStorageCore;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.teamfruit.ezstorage2patch.imixin.IGuiStorageCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class JEIGuiHandler implements IAdvancedGuiHandler<GuiStorageCore> {

    @Override
    public @NotNull Class<GuiStorageCore> getGuiContainerClass() {
        return GuiStorageCore.class;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(@NotNull GuiStorageCore guiStorageCore, int mouseX, int mouseY) {
        IGuiStorageCore mixinGuiStorageCore = ((IGuiStorageCore) (Object) guiStorageCore);
        Integer slot = mixinGuiStorageCore.invokeGetSlotAt(mouseX, mouseY);
        if (slot == null || mixinGuiStorageCore.getFilteredList().size() <= slot || mixinGuiStorageCore.isSearchFieldFocused()) {
            return null;
        }
        return mixinGuiStorageCore.getFilteredList().get(slot).itemStack;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@NotNull GuiStorageCore guiStorageCore) {
        return ((IGuiStorageCore) (Object) guiStorageCore).getJEIExclusionArea();
    }
}
