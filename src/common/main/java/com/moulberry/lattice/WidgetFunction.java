package com.moulberry.lattice;

import com.moulberry.lattice.annotation.widget.LatticeWidgetMessage;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.lattice.widget.CenteredStringWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface WidgetFunction {

    @Nullable
    AbstractWidget createWidget(Font font, @NotNull Component title, @Nullable Component description, int width);

    static WidgetFunction onOffButton(BooleanSupplier initial, Consumer<Boolean> setter) {
        return (font, title, description, width) -> CycleButton.onOffBuilder().withInitialValue(initial.getAsBoolean()).create(0, 0, width, 20, title, (cycleButton, bool) -> {
            setter.accept(bool);
        });
    }

    @SafeVarargs
    static <T> WidgetFunction cycleButton(Supplier<T> initial, Consumer<T> setter, T... values) {
        return (font, title, description, width) -> CycleButton.<T>builder(v -> Component.literal(v.toString()))
            .withValues(values)
            .withInitialValue(initial.get())
            .create(0, 0, width, 20, title, (btn, object) -> {
                setter.accept(object);
            });
    }

    static @NotNull WidgetFunction runnableButton(Runnable runnable) {
        return (font, title, description, width) -> {
            return Button.builder(title, btn -> runnable.run()).bounds(0, 0, width, 20).build();
        };
    }

    static @NotNull WidgetFunction editBox(Supplier<String> initial, Consumer<String> setter, int maxLength) {
        return (font, title, description, width) -> {
            String initialValue = initial.get();
            EditBox editBox = new EditBox(font, 0, 0, width, 20, title);
            editBox.setMaxLength(maxLength);
            editBox.setValue(initialValue);
            editBox.setResponder(setter);
            return editBox;
        };
    }

    static @NotNull WidgetFunction multilineEditBox(Supplier<String> initial, Consumer<String> setter, int height, int characterLimit) {
        return (font, title, description, width) -> {
            String initialValue = initial.get();
            MultiLineEditBox editBox = LatticeMultiversion.newMultiLineEditBox(font, width, height, title);
            editBox.setValue(initialValue);
            editBox.setCharacterLimit(characterLimit);
            editBox.setValueListener(setter);
            return editBox;
        };
    }

    static @NotNull WidgetFunction string(int maxRows, boolean centered) {
        return (font, title, description, width) -> {
            if (maxRows <= 1) {
                if (centered) {
                    return new CenteredStringWidget(0, 0, width, font.lineHeight, title, font);
                } else {
                    return new StringWidget(0, 0, width, font.lineHeight, title, font);
                }
            } else {
                MultiLineTextWidget textWidget = new MultiLineTextWidget(title, font);
                textWidget.setMaxWidth(width);
                textWidget.setMaxRows(maxRows);
                textWidget.setCentered(centered);
                return textWidget;
            }
        };
    }

}
