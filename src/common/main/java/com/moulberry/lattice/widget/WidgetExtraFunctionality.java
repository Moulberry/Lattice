package com.moulberry.lattice.widget;

import com.moulberry.lattice.element.LatticeElements;
import com.moulberry.lattice.keybind.LatticeInputType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@ApiStatus.Internal
public interface WidgetExtraFunctionality {

    @Nullable
    default GuiEventListener getPopup() {
        return null;
    }

    default boolean listeningForRawKeyInput() {
        return false;
    }

    default boolean handleRawInput(LatticeInputType inputType, int value, boolean release) {
        return false;
    }

    default void afterRawInputHandledByAny() {
    }

    default List<AbstractWidget> extraWidgets() {
        return List.of();
    }

    default int extraWidgetHorizonalOffset() {
        return 0;
    }

    default int renderVerticalLineForExtraWidgetsAtX() {
        return -1;
    }

    default @Nullable List<LatticeElements> switchToCategoryAfterClick() {
        return null;
    }


}
