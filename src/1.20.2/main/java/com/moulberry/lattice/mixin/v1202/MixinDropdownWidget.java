package com.moulberry.lattice.mixin.v1202;

import com.moulberry.lattice.widget.DropdownWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

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
            public DropdownSelection(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
                super(minecraft, width, height, y0, y1, itemHeight);
                this.setRenderBackground(false);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.fill(
                        this.x0,
                        this.y0,
                        this.x1,
                        this.y1,
                        0xF0101010
                );

                guiGraphics.renderOutline(
                        this.x0,
                        this.y0-1,
                        this.getWidth(),
                        this.getHeight()+2,
                        isFocused.getAsBoolean() ? 0xFFFFFFFF : 0xFF000000
                );

                super.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public int getRowWidth() {
                return Math.max(52, this.getWidth()) - 52;
            }

            @Override
            public void replaceEntries(Collection<DropdownWidget<T>.Entry> collection) {
                super.replaceEntries(collection);
            }

            @Override
            public void setWidth(int width) {
                this.width = width;
                this.x1 = this.x0 + width;
            }

            @Override
            public void setFocused(DropdownWidget<T>.Entry entry) {
                super.setFocused(entry);
            }

            @Override
            public void setX(int x) {
                this.x0 = x;
                this.x1 = x + this.width;
            }

            @Override
            public void setY(int y) {
                this.y0 = y;
                this.y1 = y + this.height;
            }

            @Override
            public int getX() {
                return this.x0;
            }

            @Override
            public int getY() {
                return this.y0;
            }

            @Override
            public int getWidth() {
                return this.width;
            }

            @Override
            public int getHeight() {
                return this.height;
            }

            @Override
            protected int getScrollbarPosition() {
                return this.getRowRight() + 8;
            }

            @Override
            public void visitWidgets(Consumer<AbstractWidget> consumer) {
            }
        }

        return new DropdownSelection<T>(Minecraft.getInstance(), width, height, y, y+height, itemHeight);
    }


}
