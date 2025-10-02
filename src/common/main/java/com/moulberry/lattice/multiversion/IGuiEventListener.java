package com.moulberry.lattice.multiversion;

import java.util.function.BooleanSupplier;

public interface IGuiEventListener {

    default boolean lattice$mouseClicked(IMouseButtonEvent event, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }
    default boolean lattice$mouseReleased(IMouseButtonEvent event, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }
    default boolean lattice$mouseDragged(IMouseButtonEvent event, double dx, double dy, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }
    default boolean lattice$keyPressed(IKeyEvent event, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }
    default boolean lattice$keyReleased(IKeyEvent event, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }
    default boolean lattice$charTyped(ICharacterEvent event, BooleanSupplier callSuper) {
        return callSuper.getAsBoolean();
    }

}
