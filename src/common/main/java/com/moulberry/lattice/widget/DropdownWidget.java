package com.moulberry.lattice.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.Internal
public abstract class DropdownWidget<T> extends Button implements WidgetExtraFunctionality {

    private final Component title;
    private final Font font;
    private T currentValue;

    private boolean showingSelectionDropdown = false;
    private final DropdownSelection selection;
    private final Map<T, Entry> entryByValue = new HashMap<>();

    public DropdownWidget(int x, int y, int width, int height, Font font, Component title, T initialValue, T... values) {
        super(x, y, width, height, CommonComponents.EMPTY, button -> ((DropdownWidget<T>)button).handlePress(), Supplier::get);
        this.font = font;
        this.title = title;
        this.currentValue = initialValue;

        int itemHeight = font.lineHeight + 2;
        int contentHeight = values.length * itemHeight + 4;
        int selectionDropdownHeight = Math.min(contentHeight, 100);
        this.selection = new DropdownSelection(Minecraft.getInstance(), width, selectionDropdownHeight, y, itemHeight);

        List<Entry> entries = new ArrayList<>(values.length);
        for (T value : values) {
            var entry = new Entry(value);
            entries.add(entry);
            this.entryByValue.put(value, entry);
        }
        this.selection.replaceEntries(entries);

        this.updateMessage();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
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
        this.setMessage(Component.translatable("options.generic_value", this.title, this.currentValue.toString()));
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

    private class DropdownSelection extends ObjectSelectionList<Entry> {
        public DropdownSelection(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        private boolean replaceEntries = false;

        @Override
        protected void extractListBackground(GuiGraphicsExtractor guiGraphics) {
            guiGraphics.fill(
                this.getX(),
                this.getY(),
                this.getRight(),
                this.getBottom(),
                0xF0101010
            );

            int minX = this.getX();
            int minY = this.getY()-1;
            int maxX = minX+this.getWidth();
            int maxY = minY+this.getHeight()+2;
            int colour = DropdownWidget.super.isFocused() ? 0xFFFFFFFF : 0xFF000000;
            guiGraphics.fill(minX, minY, maxX, minY + 1, colour);
            guiGraphics.fill(minX, maxY - 1, maxX, maxY, colour);
            guiGraphics.fill(minX, minY + 1, minX + 1, maxY - 1, colour);
            guiGraphics.fill(maxX - 1, minY + 1, maxX, maxY - 1, colour);

            if (this.replaceEntries) {
                this.replaceEntries = false;
                this.replaceEntries(new ArrayList<>(this.children()));
            }
        }

        @Override
        public void setX(int x) {
            this.replaceEntries |= x != this.getX();
            super.setX(x);
        }

        @Override
        public void setY(int y) {
            this.replaceEntries |= y != this.getY();
            super.setY(y);
        }

        @Override
        protected void extractListSeparators(GuiGraphicsExtractor guiGraphics) {
        }

        @Override
        public int getRowWidth() {
            return Math.max(52, this.getWidth()) - 52;
        }

        @Override
        protected int scrollBarX() {
            return this.getRowRight() + 8;
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
        }
    }

    public class Entry extends ObjectSelectionList.Entry<Entry> {
        final T value;
        private long lastClickedMillis;

        public Entry(final T value) {
            this.value = value;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            graphics.text(DropdownWidget.this.font,
                Component.literal(this.value.toString()), this.getX(), this.getY(), -1);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.isSelection()) {
                DropdownWidget.this.updateValue(this.value);
                DropdownWidget.this.showingSelectionDropdown = false;
            }
            return true;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            DropdownWidget.this.updateValue(this.value);

            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickedMillis < 250L) {
                DropdownWidget.this.showingSelectionDropdown = false;
            }
            this.lastClickedMillis = currentTime;

            super.mouseClicked(event, doubleClick);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.value);
        }
    }
}
