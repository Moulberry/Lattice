package com.moulberry.lattice.mixin.v1219;

import com.moulberry.lattice.LatticeConfigScreen;
import com.moulberry.lattice.multiversion.*;
import com.moulberry.lattice.widget.CategoryStringWidget;
import com.moulberry.lattice.widget.DropdownWidget;
import com.moulberry.lattice.widget.EditableSlider;
import com.moulberry.lattice.widget.WidgetWithText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

public abstract class GuiEventListenerMixins {

    @Mixin(MouseButtonEvent.class)
    public static class MixinMouseButtonEvent implements IMouseButtonEvent {
        @Override
        public int lattice$button() {
            return ((MouseButtonEvent)(Object)this).button();
        }

        @Override
        public double lattice$x() {
            return ((MouseButtonEvent)(Object)this).x();
        }

        @Override
        public double lattice$y() {
            return ((MouseButtonEvent)(Object)this).y();
        }

        @Override
        public boolean lattice$hasShiftDown() {
            return ((KeyEvent)(Object)this).hasShiftDown();
        }

        @Override
        public boolean lattice$hasCtrlOrCmdDown() {
            return ((KeyEvent)(Object)this).hasControlDown();
        }

        @Override
        public boolean lattice$passClickedTo(GuiEventListener eventListener) {
            return eventListener.mouseClicked((MouseButtonEvent)(Object)this, false);
        }

        @Override
        public boolean lattice$passReleasedTo(GuiEventListener eventListener) {
            return eventListener.mouseReleased((MouseButtonEvent)(Object)this);
        }

        @Override
        public boolean lattice$passDraggedTo(GuiEventListener eventListener, double dx, double dy) {
            return eventListener.mouseDragged((MouseButtonEvent)(Object)this, dx, dy);
        }
    }

    @Mixin(KeyEvent.class)
    public static class MixinKeyEvent implements IKeyEvent {
        @Override
        public int lattice$keysym() {
            return ((KeyEvent)(Object)this).key();
        }

        @Override
        public int lattice$scancode() {
            return ((KeyEvent)(Object)this).scancode();
        }

        @Override
        public int lattice$modifiers() {
            return ((KeyEvent)(Object)this).modifiers();
        }

        @Override
        public boolean lattice$hasShiftDown() {
            return ((KeyEvent)(Object)this).hasShiftDown();
        }

        @Override
        public boolean lattice$hasCtrlOrCmdDown() {
            return ((KeyEvent)(Object)this).hasControlDown();
        }

        @Override
        public boolean lattice$isSelection() {
            return ((KeyEvent)(Object)this).isSelection();
        }

        @Override
        public boolean lattice$passPressedTo(GuiEventListener eventListener) {
            return eventListener.keyPressed((KeyEvent)(Object)this);
        }

        @Override
        public boolean lattice$passReleasedTo(GuiEventListener eventListener) {
            return eventListener.keyReleased((KeyEvent)(Object)this);
        }
    }

    @Mixin(CharacterEvent.class)
    public static class MixinCharacterEvent implements ICharacterEvent {
        @Override
        public boolean lattice$passCharTypedTo(GuiEventListener eventListener) {
            return eventListener.charTyped((CharacterEvent)(Object)this);
        }
    }

    @Mixin(LatticeConfigScreen.class)
    public static abstract class MixinLatticeConfigScreen extends Screen implements IGuiEventListener {
        protected MixinLatticeConfigScreen() {
            super(CommonComponents.EMPTY);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent vanillaMouseButtonEvent, boolean doubleClick) {
            return this.lattice$mouseClicked((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseClicked(vanillaMouseButtonEvent, doubleClick));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent vanillaMouseButtonEvent) {
            return this.lattice$mouseReleased((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseReleased(vanillaMouseButtonEvent));
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent vanillaMouseButtonEvent, double dx, double dy) {
            return this.lattice$mouseDragged((IMouseButtonEvent)(Object)vanillaMouseButtonEvent, dx, dy,
                    () -> super.mouseDragged(vanillaMouseButtonEvent, dx, dy));
        }

        @Override
        public boolean keyPressed(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyPressed((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyPressed(vanillaKeyEvent));
        }

        @Override
        public boolean keyReleased(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyReleased((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyReleased(vanillaKeyEvent));
        }

        @Override
        public boolean charTyped(CharacterEvent vanillaCharacterEvent) {
            return this.lattice$charTyped((ICharacterEvent)(Object)vanillaCharacterEvent, () -> super.charTyped(vanillaCharacterEvent));
        }
    }

    @Mixin({WidgetWithText.class, CategoryStringWidget.class})
    public static abstract class MixinAbstractWidgets extends AbstractWidget implements IGuiEventListener {
        public MixinAbstractWidgets(int $$0, int $$1, int $$2, int $$3, Component $$4) {
            super($$0, $$1, $$2, $$3, $$4);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent vanillaMouseButtonEvent, boolean doubleClick) {
            return this.lattice$mouseClicked((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseClicked(vanillaMouseButtonEvent, doubleClick));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent vanillaMouseButtonEvent) {
            return this.lattice$mouseReleased((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseReleased(vanillaMouseButtonEvent));
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent vanillaMouseButtonEvent, double dx, double dy) {
            return this.lattice$mouseDragged((IMouseButtonEvent)(Object)vanillaMouseButtonEvent, dx, dy,
                    () -> super.mouseDragged(vanillaMouseButtonEvent, dx, dy));
        }

        @Override
        public boolean keyPressed(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyPressed((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyPressed(vanillaKeyEvent));
        }

        @Override
        public boolean keyReleased(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyReleased((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyReleased(vanillaKeyEvent));
        }

        @Override
        public boolean charTyped(CharacterEvent vanillaCharacterEvent) {
            return this.lattice$charTyped((ICharacterEvent)(Object)vanillaCharacterEvent, () -> super.charTyped(vanillaCharacterEvent));
        }
    }

    @Mixin(DropdownWidget.Entry.class)
    public static abstract class MixinDropdownWidget extends ObjectSelectionList.Entry implements IGuiEventListener {
        @Override
        public boolean mouseClicked(MouseButtonEvent vanillaMouseButtonEvent, boolean doubleClick) {
            return this.lattice$mouseClicked((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseClicked(vanillaMouseButtonEvent, doubleClick));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent vanillaMouseButtonEvent) {
            return this.lattice$mouseReleased((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseReleased(vanillaMouseButtonEvent));
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent vanillaMouseButtonEvent, double dx, double dy) {
            return this.lattice$mouseDragged((IMouseButtonEvent)(Object)vanillaMouseButtonEvent, dx, dy,
                    () -> super.mouseDragged(vanillaMouseButtonEvent, dx, dy));
        }

        @Override
        public boolean keyPressed(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyPressed((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyPressed(vanillaKeyEvent));
        }

        @Override
        public boolean keyReleased(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyReleased((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyReleased(vanillaKeyEvent));
        }

        @Override
        public boolean charTyped(CharacterEvent vanillaCharacterEvent) {
            return this.lattice$charTyped((ICharacterEvent)(Object)vanillaCharacterEvent, () -> super.charTyped(vanillaCharacterEvent));
        }
    }

    @Mixin(EditableSlider.class)
    public static abstract class MixinEditableSlider extends AbstractSliderButton implements IGuiEventListener {
        public MixinEditableSlider(int $$0, int $$1, int $$2, int $$3, Component $$4, double $$5) {
            super($$0, $$1, $$2, $$3, $$4, $$5);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent vanillaMouseButtonEvent, boolean doubleClick) {
            return this.lattice$mouseClicked((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseClicked(vanillaMouseButtonEvent, doubleClick));
        }

        @Override
        public boolean mouseReleased(MouseButtonEvent vanillaMouseButtonEvent) {
            return this.lattice$mouseReleased((IMouseButtonEvent)(Object)vanillaMouseButtonEvent,
                    () -> super.mouseReleased(vanillaMouseButtonEvent));
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent vanillaMouseButtonEvent, double dx, double dy) {
            return this.lattice$mouseDragged((IMouseButtonEvent)(Object)vanillaMouseButtonEvent, dx, dy,
                    () -> super.mouseDragged(vanillaMouseButtonEvent, dx, dy));
        }

        @Override
        public boolean keyPressed(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyPressed((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyPressed(vanillaKeyEvent));
        }

        @Override
        public boolean keyReleased(KeyEvent vanillaKeyEvent) {
            return this.lattice$keyReleased((IKeyEvent)(Object)vanillaKeyEvent, () -> super.keyReleased(vanillaKeyEvent));
        }

        @Override
        public boolean charTyped(CharacterEvent vanillaCharacterEvent) {
            return this.lattice$charTyped((ICharacterEvent)(Object)vanillaCharacterEvent, () -> super.charTyped(vanillaCharacterEvent));
        }
    }

}
