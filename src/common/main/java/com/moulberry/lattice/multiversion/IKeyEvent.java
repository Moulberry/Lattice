package com.moulberry.lattice.multiversion;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IKeyEvent {

    int lattice$keysym();
    int lattice$scancode();
    int lattice$modifiers();
    boolean lattice$hasShiftDown();
    boolean lattice$hasCtrlOrCmdDown();
    boolean lattice$isSelection();

    boolean lattice$passPressedTo(GuiEventListener eventListener);
    boolean lattice$passReleasedTo(GuiEventListener eventListener);

}
