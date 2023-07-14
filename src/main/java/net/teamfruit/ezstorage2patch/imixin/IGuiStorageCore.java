package net.teamfruit.ezstorage2patch.imixin;

import com.zerofall.ezstorage.util.ItemGroup;

import java.util.List;

public interface IGuiStorageCore {

    List<ItemGroup> getFilteredList();

    Integer invokeGetSlotAt(int x, int y);

    boolean isSearchFieldFocused();
}
