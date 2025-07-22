package com.moulberry.lattice.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@ApiStatus.Internal
public abstract class EditableSlider<T> extends AbstractSliderButton {

    private final EditBox editBox;
    private final Component title;
    private final boolean allowManualInput;
    private long lastEditBoxDefocusMillis = 0;
    private T realValue;

    private String formattingString = null;

    public EditableSlider(int x, int y, int width, int height, Component title, Font font, boolean allowManualInput, T initial) {
        super(x, y, width, height, CommonComponents.EMPTY, 0);
        this.value = toSliderRange(initial);
        this.editBox = new EditBox(font, x, y, width, height, title);
        this.title = title;
        this.allowManualInput = allowManualInput;
        this.realValue = initial;
        this.updateMessage();
    }

    public abstract double toSliderRange(T value);
    public abstract T fromSliderRange(double value);
    public abstract T fromString(String value);
    public abstract T clampValue(T value);
    public abstract void setValue(T value);

    public void setFormattingString(String formattingString) {
        if (!Objects.equals(this.formattingString, formattingString)) {
            this.formattingString = formattingString;
            this.updateMessage();
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.editBox.isFocused()) {
            this.editBox.renderWidget(guiGraphics, i, j, f);
        } else {
            super.renderWidget(guiGraphics, i, j, f);
        }
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.editBox.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.editBox.setY(y);
    }

    @Override
    public void setWidth(int w) {
        super.setWidth(w);
        this.editBox.setWidth(w);
    }

    @Override
    public boolean mouseClicked(double d, double e, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || (Screen.hasControlDown() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (this.allowManualInput && !this.editBox.isFocused()) {
                setEditBoxFocus(true);
                return true;
            }
        }
        if (this.editBox.isFocused()) {
            return this.editBox.mouseClicked(d, e, button);
        }
        return super.mouseClicked(d, e, button);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (this.editBox.isFocused()) {
            if (i == GLFW.GLFW_KEY_ESCAPE || i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) {
                setEditBoxFocus(false);
                return true;
            }
            boolean handled = this.editBox.keyPressed(i, j, k);

            T value;
            try {
                value = this.clampValue(this.fromString(this.editBox.getValue()));
            } catch (Exception e) {
                this.editBox.setTextColor(0xFFFF0000);
                return handled;
            }

            if (this.realValue != value) {
                this.realValue = value;
                this.value = Math.max(0, Math.min(1, this.toSliderRange(value)));
                this.setValue(value);
            }

            this.editBox.setTextColor(0xFFFFFFFF);
            return handled;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.editBox.isFocused()) {
            boolean handled = this.editBox.charTyped(c, i);

            T value;
            try {
                value = this.clampValue(this.fromString(this.editBox.getValue()));
            } catch (Exception e) {
                this.editBox.setTextColor(0xFFFF0000);
                return handled;
            }

            if (this.realValue != value) {
                this.realValue = value;
                this.value = Math.max(0, Math.min(1, this.toSliderRange(value)));
                this.setValue(value);
            }

            this.editBox.setTextColor(0xFFFFFFFF);
            return handled;
        }
        return super.charTyped(c, i);
    }

    @Override
    protected void updateMessage() {
        Object formatted = this.realValue;
        if (this.formattingString != null) {
            formatted = String.format(this.formattingString, this.realValue);
        }
        this.setMessage(Component.translatable("options.generic_value", this.title, formatted));
    }

    @Override
    protected void applyValue() {
        T value = this.clampValue(this.fromSliderRange(this.value));
        if (this.realValue != value) {
            this.realValue = value;
            this.setValue(this.realValue);
        }
    }

    @Override
    public boolean isFocused() {
        return super.isFocused() || this.editBox.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && this.editBox.isFocused()) {
            setEditBoxFocus(false);
            lastEditBoxDefocusMillis = System.currentTimeMillis();
        }
        if (focused && System.currentTimeMillis() - lastEditBoxDefocusMillis <= 50L) {
            lastEditBoxDefocusMillis = 0L;
            setEditBoxFocus(true);
        }
    }

    private void setEditBoxFocus(boolean focused) {
        if (focused == this.editBox.isFocused()) {
            return;
        }
        if (focused) {
            this.editBox.setValue(this.realValue.toString());
            this.editBox.setTextColor(0xFFFFFFFF);
            this.editBox.setFocused(true);
        } else {
            this.updateMessage();
            this.editBox.setFocused(false);
        }
    }

}
