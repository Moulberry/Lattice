package com.moulberry.lattice.mixin.v1206;

import com.moulberry.lattice.widget.DropdownWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Mixin(DropdownWidget.class)
public class MixinDropdownWidget {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    private static <T> DropdownWidget.DropdownSelectionInterface<T> createDropdownSelection(int width, int height, int y, int itemHeight, BooleanSupplier isFocused) {
        class DropdownSelection<T> extends ObjectSelectionList<DropdownWidget<T>.Entry> implements DropdownWidget.DropdownSelectionInterface<T> {
            public DropdownSelection(Minecraft minecraft, int width, int height, int y, int itemHeight) {
                super(minecraft, width, height, y, itemHeight);
            }

            private boolean replaceEntries = false;

            protected void renderListBackground(GuiGraphics guiGraphics) {
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
                int colour = isFocused.getAsBoolean() ? 0xFFFFFFFF : 0xFF000000;
                guiGraphics.fill(minX, minY, maxX, minY + 1, colour);
                guiGraphics.fill(minX, maxY - 1, maxX, maxY, colour);
                guiGraphics.fill(minX, minY + 1, minX + 1, maxY - 1, colour);
                guiGraphics.fill(maxX - 1, minY + 1, maxX, maxY - 1, colour);

                // Needed on 1.21.9+ to update position of widgets
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

            protected void renderListSeparators(GuiGraphics guiGraphics) {
            }

            public int getRowWidth() {
                return Math.max(52, this.getWidth()) - 52;
            }

            @Override
            public void replaceEntries(Collection<DropdownWidget<T>.Entry> collection) {
                super.replaceEntries(collection);
            }

            @Override
            public void setFocused(DropdownWidget<T>.Entry entry) {
                super.setFocused(entry);
            }

            protected int getScrollbarPosition() {
                return this.getRowRight() + 8;
            }

            public void visitWidgets(Consumer<AbstractWidget> consumer) {
            }
        }

        return new DropdownSelection<T>(Minecraft.getInstance(), width, height, y, itemHeight);
    }


}
