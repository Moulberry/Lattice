package com.moulberry.lattice.v1201;

import com.moulberry.lattice.multiversion.ICharacterEvent;
import com.moulberry.lattice.multiversion.IKeyEvent;
import com.moulberry.lattice.multiversion.IMouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public class LegacyInputEvents {

    public record LegacyMouseButtonEvent(double x, double y, int button) implements IMouseButtonEvent {
        @Override
        public int lattice$button() {
            return this.button;
        }

        @Override
        public double lattice$x() {
            return this.x;
        }

        @Override
        public double lattice$y() {
            return this.y;
        }

        @Override
        public boolean lattice$hasShiftDown() {
            return Screen.hasShiftDown();
        }

        @Override
        public boolean lattice$hasCtrlOrCmdDown() {
            return Screen.hasControlDown();
        }

        @Override
        public boolean lattice$passClickedTo(GuiEventListener eventListener) {
            return eventListener.mouseClicked(this.x, this.y, this.button);
        }

        @Override
        public boolean lattice$passReleasedTo(GuiEventListener eventListener) {
            return eventListener.mouseReleased(this.x, this.y, this.button);
        }

        @Override
        public boolean lattice$passDraggedTo(GuiEventListener eventListener, double dx, double dy) {
            return eventListener.mouseDragged(this.x, this.y, this.button, dx, dy);
        }
    }

    public record LegacyKeyEvent(int key, int scancode, int modifiers) implements IKeyEvent {
        @Override
        public int lattice$keysym() {
            return this.key;
        }

        @Override
        public int lattice$scancode() {
            return this.scancode;
        }

        @Override
        public int lattice$modifiers() {
            return this.modifiers;
        }

        @Override
        public boolean lattice$hasShiftDown() {
            return (this.modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        }

        @Override
        public boolean lattice$hasCtrlOrCmdDown() {
            return Minecraft.ON_OSX ? (this.modifiers & GLFW.GLFW_MOD_SUPER) != 0 : (this.modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        }

        @Override
        public boolean lattice$isSelection() {
            return CommonInputs.selected(this.key);
        }

        @Override
        public boolean lattice$passPressedTo(GuiEventListener eventListener) {
            return eventListener.keyPressed(this.key, this.scancode, this.modifiers);
        }

        @Override
        public boolean lattice$passReleasedTo(GuiEventListener eventListener) {
            return eventListener.keyReleased(this.key, this.scancode, this.modifiers);
        }
    }

    public record LegacyCharacterEvent(char character, int modifiers) implements ICharacterEvent {
        @Override
        public boolean lattice$passCharTypedTo(GuiEventListener eventListener) {
            return eventListener.charTyped(this.character, this.modifiers);
        }
    }

}
