package com.moulberry.lattice.widget;

import com.moulberry.lattice.multiversion.IGuiEventListener;
import com.moulberry.lattice.multiversion.IKeyEvent;
import com.moulberry.lattice.multiversion.IMouseButtonEvent;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class DropdownWidget<T> extends Button implements WidgetExtraFunctionality {

    private final Component title;
    private final Font font;
    private T currentValue;
    private final T[] values;

    private boolean showingSelectionDropdown = false;
    private final DropdownSelectionInterface<T> selection;
    private final Map<T, Entry> entryByValue = new HashMap<>();

    public DropdownWidget(int x, int y, int width, int height, Font font, Component title, T initialValue, T... values) {
        super(x, y, width, height, CommonComponents.EMPTY, button -> ((DropdownWidget<T>)button).handlePress(), Supplier::get);
        this.font = font;
        this.title = title;
        this.currentValue = initialValue;
        this.values = values;

        int itemHeight = font.lineHeight + 2;
        int contentHeight = values.length * itemHeight + 4;
        int selectionDropdownHeight = Math.min(contentHeight, 100);
        this.selection = createDropdownSelection(width, selectionDropdownHeight, y, itemHeight, this::isFocused);

        List<Entry> entries = new ArrayList<>(values.length);
        for (T value : values) {
            var entry = new Entry(value);
            entries.add(entry);
            this.entryByValue.put(value, entry);
        }
        this.selection.replaceEntries(entries);

        this.updateMessage();
    }

    public abstract void setValue(T value);

    public void updateValue(T value) {
        this.currentValue = value;
        this.updateMessage();
        this.setValue(value);
    }

    @Override
    public @Nullable GuiEventListener getPopup() {
        return this.showingSelectionDropdown ? this.selection : null;
    }

    private void updateMessage() {
        this.setMessage(Component.translatable("options.generic_value", this.title, this.currentValue));
    }

    @Override
    public void setWidth(int w) {
        super.setWidth(w);
        this.selection.setWidth(w);
    }

    public void handlePress() {
        if (!this.showingSelectionDropdown) {
            this.showingSelectionDropdown = true;

            var currentEntry = this.entryByValue.get(this.currentValue);
            if (currentEntry != null) {
                this.selection.setFocused(currentEntry);
            }
        }
    }

    public interface DropdownSelectionInterface<T> extends GuiEventListener, LayoutElement {
        void replaceEntries(Collection<DropdownWidget<T>.Entry> collection);
        void setWidth(int width);
        void setFocused(DropdownWidget<T>.Entry entry);

        @Override
        default ScreenRectangle getRectangle() {
            return LayoutElement.super.getRectangle();
        }
    }

    private static <T> DropdownSelectionInterface<T> createDropdownSelection(int width, int height, int y, int itemHeight, BooleanSupplier isFocused) {
        // Implemented by MixinDropdownWidget
        throw new UnsupportedOperationException();
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> implements IGuiEventListener {
        final T value;
        private long lastClickedMillis;

        public Entry(final T value) {
            this.value = value;
        }

        // For <1.21.9
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            this.actuallyRender(guiGraphics, x, y);
        }

        // For >=1.21.9
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            throw new UnsupportedOperationException("Implemented by MixinDropdownWidgetEntry");
        }

        public void actuallyRender(GuiGraphics guiGraphics, int x, int y) {
            LatticeMultiversion.drawString(guiGraphics, DropdownWidget.this.font,
                    Component.literal(this.value.toString()), x, y, -1);
        }

        @Override
        public boolean lattice$keyPressed(IKeyEvent event, BooleanSupplier callSuper) {
            if (event.lattice$isSelection()) {
                DropdownWidget.this.updateValue(this.value);
                DropdownWidget.this.showingSelectionDropdown = false;
            }
            return true;
        }

        @Override
        public boolean lattice$mouseClicked(IMouseButtonEvent event, BooleanSupplier callSuper) {
            DropdownWidget.this.updateValue(this.value);

            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickedMillis < 250L) {
                DropdownWidget.this.showingSelectionDropdown = false;
            }
            this.lastClickedMillis = currentTime;

            callSuper.getAsBoolean();
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.value);
        }
    }
}
