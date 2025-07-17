package com.moulberry.lattice.testmod;

import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.lattice.keybind.KeybindInterface;
import com.moulberry.lattice.keybind.LatticeInputType;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;

public class CustomKeybind implements KeybindInterface {

    private LatticeInputType type;
    private int value;
    private boolean shiftMod;
    private boolean ctrlMod;
    private boolean altMod;
    private boolean superMod;

    public CustomKeybind(LatticeInputType type, int value, boolean shiftMod, boolean ctrlMod, boolean altMod, boolean superMod) {
        this.type = type;
        this.value = value;
        this.shiftMod = shiftMod;
        this.ctrlMod = ctrlMod;
        this.altMod = altMod;
        this.superMod = superMod;
    }

    @Override
    public Component getKeyMessage() {
        if (this.type == null) {
            return Component.literal("None");
        }

        StringBuilder message = new StringBuilder();
        if (this.shiftMod) {
            message.append("Shift+");
        }
        if (this.ctrlMod) {
            message.append("Ctrl+");
        }
        if (this.altMod) {
            message.append("Alt+");
        }
        if (this.superMod) {
            message.append("Super+");
        }

        Component name = switch (this.type) {
            case KEYSYM -> InputConstants.Type.KEYSYM.getOrCreate(this.value).getDisplayName();
            case SCANCODE -> InputConstants.Type.SCANCODE.getOrCreate(this.value).getDisplayName();
            case MOUSE -> InputConstants.Type.MOUSE.getOrCreate(this.value).getDisplayName();
        };

        return Component.literal(message.toString()).append(name);
    }

    @Override
    public void setKey(LatticeInputType type, int value, boolean shiftMod, boolean ctrlMod, boolean altMod, boolean superMod) {
        this.type = type;
        this.value = value;
        this.shiftMod = shiftMod;
        this.ctrlMod = ctrlMod;
        this.altMod = altMod;
        this.superMod = superMod;
    }

    @Override
    public void setUnbound() {
        this.type = null;
    }

    @Override
    public Collection<Component> getConflicts() {
        return List.of();
    }
}
