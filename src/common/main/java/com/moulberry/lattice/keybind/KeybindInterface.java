package com.moulberry.lattice.keybind;

import net.minecraft.network.chat.Component;

import java.util.Collection;

public interface KeybindInterface {
    Component getKeyMessage();

    void setKey(LatticeInputType type, int value, boolean shiftMod, boolean ctrlMod, boolean altMod, boolean superMod);
    void setUnbound();

    Collection<Component> getConflicts();

}
