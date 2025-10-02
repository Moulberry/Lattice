package com.moulberry.lattice.multiversion;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IMouseButtonEvent {

    int lattice$button();
    double lattice$x();
    double lattice$y();
    boolean lattice$hasShiftDown();
    boolean lattice$hasCtrlOrCmdDown();

    boolean lattice$passClickedTo(GuiEventListener eventListener);
    boolean lattice$passReleasedTo(GuiEventListener eventListener);
    boolean lattice$passDraggedTo(GuiEventListener eventListener, double dx, double dy);

}
