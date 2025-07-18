package com.moulberry.lattice.mixin.v1204;

import com.moulberry.lattice.widget.DropdownWidget;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
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
@IfMinecraftVersion(minVersion = "1.20.3", maxVersion = "1.20.4")
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
                this.setRenderBackground(false);
            }

            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.fill(
                        this.getX(),
                        this.getY(),
                        this.getRight(),
                        this.getBottom(),
                        0xF0101010
                );

                guiGraphics.renderOutline(
                        this.getX(),
                        this.getY()-1,
                        this.getWidth(),
                        this.getHeight()+2,
                        isFocused.getAsBoolean() ? 0xFFFFFFFF : 0xFF000000
                );

                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
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
            public void setFocused(DropdownWidget<T>.Entry entry) {
                super.setFocused(entry);
            }

            @Override
            protected int getScrollbarPosition() {
                return this.getRowRight() + 8;
            }

            @Override
            public void visitWidgets(Consumer<AbstractWidget> consumer) {
            }
        }

        return new DropdownSelection<T>(Minecraft.getInstance(), width, height, y, itemHeight);
    }


}
