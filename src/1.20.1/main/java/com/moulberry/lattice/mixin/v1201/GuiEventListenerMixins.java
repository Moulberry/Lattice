package com.moulberry.lattice.mixin.v1201;

import com.moulberry.lattice.LatticeConfigScreen;
import com.moulberry.lattice.multiversion.IGuiEventListener;
import com.moulberry.lattice.v1201.LegacyInputEvents;
import com.moulberry.lattice.widget.CategoryStringWidget;
import com.moulberry.lattice.widget.DropdownWidget;
import com.moulberry.lattice.widget.EditableSlider;
import com.moulberry.lattice.widget.WidgetWithText;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

public abstract class GuiEventListenerMixins {

    @Mixin(LatticeConfigScreen.class)
    public static abstract class MixinLatticeConfigScreen extends Screen implements IGuiEventListener {
        protected MixinLatticeConfigScreen() {
            super(CommonComponents.EMPTY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.lattice$mouseClicked(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseClicked(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.lattice$mouseReleased(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseReleased(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return this.lattice$mouseDragged(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button), dx, dy,
                    () -> super.mouseDragged(mouseX, mouseY, button, dx, dy));
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            return this.lattice$keyPressed(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyPressed(key, scancode, modifiers));
        }

        @Override
        public boolean keyReleased(int key, int scancode, int modifiers) {
            return this.lattice$keyReleased(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyReleased(key, scancode, modifiers));
        }

        @Override
        public boolean charTyped(char character, int modifiers) {
            return this.lattice$charTyped(new LegacyInputEvents.LegacyCharacterEvent(character, modifiers),
                    () -> super.charTyped(character, modifiers));
        }
    }

    @Mixin({WidgetWithText.class, CategoryStringWidget.class})
    public static abstract class MixinAbstractWidgets extends AbstractWidget implements IGuiEventListener {
        public MixinAbstractWidgets(int $$0, int $$1, int $$2, int $$3, Component $$4) {
            super($$0, $$1, $$2, $$3, $$4);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.lattice$mouseClicked(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseClicked(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.lattice$mouseReleased(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseReleased(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return this.lattice$mouseDragged(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button), dx, dy,
                    () -> super.mouseDragged(mouseX, mouseY, button, dx, dy));
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            return this.lattice$keyPressed(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyPressed(key, scancode, modifiers));
        }

        @Override
        public boolean keyReleased(int key, int scancode, int modifiers) {
            return this.lattice$keyReleased(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyReleased(key, scancode, modifiers));
        }

        @Override
        public boolean charTyped(char character, int modifiers) {
            return this.lattice$charTyped(new LegacyInputEvents.LegacyCharacterEvent(character, modifiers),
                    () -> super.charTyped(character, modifiers));
        }
    }

    @Mixin(DropdownWidget.Entry.class)
    public static abstract class MixinDropdownWidget extends ObjectSelectionList.Entry implements IGuiEventListener {
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.lattice$mouseClicked(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseClicked(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.lattice$mouseReleased(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseReleased(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return this.lattice$mouseDragged(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button), dx, dy,
                    () -> super.mouseDragged(mouseX, mouseY, button, dx, dy));
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            return this.lattice$keyPressed(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyPressed(key, scancode, modifiers));
        }

        @Override
        public boolean keyReleased(int key, int scancode, int modifiers) {
            return this.lattice$keyReleased(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyReleased(key, scancode, modifiers));
        }

        @Override
        public boolean charTyped(char character, int modifiers) {
            return this.lattice$charTyped(new LegacyInputEvents.LegacyCharacterEvent(character, modifiers),
                    () -> super.charTyped(character, modifiers));
        }
    }

    @Mixin(EditableSlider.class)
    public static abstract class MixinEditableSlider extends AbstractSliderButton implements IGuiEventListener {
        public MixinEditableSlider(int $$0, int $$1, int $$2, int $$3, Component $$4, double $$5) {
            super($$0, $$1, $$2, $$3, $$4, $$5);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.lattice$mouseClicked(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseClicked(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.lattice$mouseReleased(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button),
                    () -> super.mouseReleased(mouseX, mouseY, button));
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
            return this.lattice$mouseDragged(new LegacyInputEvents.LegacyMouseButtonEvent(mouseX, mouseY, button), dx, dy,
                    () -> super.mouseDragged(mouseX, mouseY, button, dx, dy));
        }

        @Override
        public boolean keyPressed(int key, int scancode, int modifiers) {
            return this.lattice$keyPressed(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyPressed(key, scancode, modifiers));
        }

        @Override
        public boolean keyReleased(int key, int scancode, int modifiers) {
            return this.lattice$keyReleased(new LegacyInputEvents.LegacyKeyEvent(key, scancode, modifiers),
                    () -> super.keyReleased(key, scancode, modifiers));
        }

        @Override
        public boolean charTyped(char character, int modifiers) {
            return this.lattice$charTyped(new LegacyInputEvents.LegacyCharacterEvent(character, modifiers),
                    () -> super.charTyped(character, modifiers));
        }
    }

}
