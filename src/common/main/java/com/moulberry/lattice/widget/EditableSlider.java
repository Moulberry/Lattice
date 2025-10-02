package com.moulberry.lattice.widget;

import com.moulberry.lattice.multiversion.ICharacterEvent;
import com.moulberry.lattice.multiversion.IGuiEventListener;
import com.moulberry.lattice.multiversion.IKeyEvent;
import com.moulberry.lattice.multiversion.IMouseButtonEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public abstract class EditableSlider<T> extends AbstractSliderButton implements IGuiEventListener {

    private final EditBox editBox;
    private final Component title;
    private final boolean allowManualInput;
    private long lastEditBoxDefocusMillis = 0;
    private T realValue;

    private String formattingString = null;
    private boolean translateFormattingString = false;

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

    public void setFormattingString(@Nullable String formattingString) {
        this.setFormattingString(formattingString, false);
    }

    public void setFormattingString(@Nullable String formattingString, boolean translate) {
        if (!Objects.equals(this.formattingString, formattingString) || (formattingString != null && this.translateFormattingString != translate)) {
            this.formattingString = formattingString;
            this.translateFormattingString = translate;
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
    public boolean lattice$mouseClicked(IMouseButtonEvent event, BooleanSupplier callSuper) {
        int button = event.lattice$button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || (event.lattice$hasCtrlOrCmdDown() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            if (this.allowManualInput && !this.editBox.isFocused()) {
                setEditBoxFocus(true);
                return true;
            }
        }
        if (this.editBox.isFocused()) {
            return event.lattice$passClickedTo(this.editBox);
        }
        return callSuper.getAsBoolean();
    }

    @Override
    public boolean lattice$keyPressed(IKeyEvent event, BooleanSupplier callSuper) {
        if (this.editBox.isFocused()) {
            int key = event.lattice$keysym();
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                setEditBoxFocus(false);
                return true;
            }
            boolean handled = event.lattice$passPressedTo(this.editBox);

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
        return callSuper.getAsBoolean();
    }

    @Override
    public boolean lattice$charTyped(ICharacterEvent event, BooleanSupplier callSuper) {
        if (this.editBox.isFocused()) {
            boolean handled = event.lattice$passCharTypedTo(this.editBox);

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
        return callSuper.getAsBoolean();
    }

    @Override
    protected void updateMessage() {
        Object formatted = this.realValue;
        if (this.formattingString != null) {
            if (this.translateFormattingString) {
                formatted = I18n.get(this.formattingString, this.realValue);
            } else {
                formatted = String.format(this.formattingString, this.realValue);
            }
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
