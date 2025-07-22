package com.moulberry.lattice.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

@ApiStatus.Internal
public abstract class DiscreteSlider<T> extends AbstractSliderButton {

    private final Component title;
    private T currentValue;
    private final T[] values;

    private String formattingString = null;
    private boolean translateFormattingString = false;

    public DiscreteSlider(int x, int y, int width, int height, Component title, T initialValue, T... values) {
        super(x, y, width, height, CommonComponents.EMPTY, calculateInitialIndex(initialValue, values));
        this.title = title;
        this.currentValue = initialValue;
        this.values = values;
        this.updateMessage();
    }

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

    public abstract void setValue(T value);

    private static double calculateInitialIndex(Object initialValue, Object[] values) {
        return initialValue == null || values.length <= 1 ? 0.0 : Arrays.asList(values).indexOf(initialValue) / (double) (values.length - 1);
    }

    @Override
    protected void updateMessage() {
        Object formatted = this.currentValue;
        if (this.formattingString != null) {
            if (this.translateFormattingString) {
                formatted = I18n.get(this.formattingString, this.currentValue);
            } else {
                formatted = String.format(this.formattingString, this.currentValue);
            }
        }
        this.setMessage(Component.translatable("options.generic_value", this.title, formatted));
    }

    @Override
    protected void applyValue() {
        if (this.values.length == 0) {
            return;
        }
        int index = (int) Math.round(this.value * (this.values.length - 1));
        index = Math.max(0, Math.min(this.values.length - 1, index));
        T value = this.values[index];
        if (this.currentValue != value) {
            this.currentValue = value;
            this.setValue(this.currentValue);
        }
    }
}
