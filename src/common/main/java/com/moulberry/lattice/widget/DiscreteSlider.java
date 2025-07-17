package com.moulberry.lattice.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

@ApiStatus.Internal
public abstract class DiscreteSlider<T> extends AbstractSliderButton {

    private final Component title;
    private T currentValue;
    private final T[] values;

    public DiscreteSlider(int x, int y, int width, int height, Component title, T initialValue, T... values) {
        super(x, y, width, height, CommonComponents.EMPTY, calculateInitialIndex(initialValue, values));
        this.title = title;
        this.currentValue = initialValue;
        this.values = values;
        this.updateMessage();
    }

    public abstract void setValue(T value);

    private static double calculateInitialIndex(Object initialValue, Object[] values) {
        return initialValue == null || values.length <= 1 ? 0.0 : Arrays.asList(values).indexOf(initialValue) / (double) (values.length - 1);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable("options.generic_value", this.title, this.currentValue));
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
