package com.moulberry.lattice.widget;

import com.moulberry.lattice.element.LatticeElements;
import com.moulberry.lattice.multiversion.IGuiEventListener;
import com.moulberry.lattice.multiversion.IMouseButtonEvent;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class CategoryStringWidget extends AbstractWidget implements IGuiEventListener, WidgetExtraFunctionality {

    private static final FormattedCharSequence SLASH = FormattedCharSequence.forward("/", Style.EMPTY);
    private final List<LatticeElements> categories;
    private final List<Component> categoryTitles;
    private final Font font;

    private List<LatticeElements> clickedCategoryPath = null;

    public CategoryStringWidget(int x, int y, int width, int height, List<LatticeElements> categories, List<Component> categoryTitles, Font font) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.categories = categories;
        this.categoryTitles = categoryTitles;
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX();
        int y = this.getY() + (this.getHeight() - this.font.lineHeight) / 2;

        boolean hoverY = mouseY >= y && mouseY <= y + this.font.lineHeight;

        int slashWidth = -1;

        for (int i = 0; i < this.categoryTitles.size(); i++) {
            Component title = this.categoryTitles.get(i);

            int width = this.font.width(title);

            if (hoverY && mouseX >= x && mouseX <= x+width && i < this.categories.size()) {
                title = Component.literal(title.getString()).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE);
            }

            LatticeMultiversion.drawString(guiGraphics, this.font, title, x, y, -1);

            if (i < this.categories.size()-1) {
                x += width + 4;
                LatticeMultiversion.drawString(guiGraphics, this.font, SLASH, x, y, -1);
                if (slashWidth == -1) {
                    slashWidth = this.font.width(SLASH);
                }
                x += 4 + slashWidth;
            }
        }
    }

    @Override
    public boolean lattice$mouseClicked(IMouseButtonEvent event, BooleanSupplier callSuper) {
        this.clickedCategoryPath = null;

        int x = this.getX();
        int y = this.getY() + (this.getHeight() - this.font.lineHeight) / 2;

        int mouseButton = event.lattice$button();
        double mouseX = event.lattice$x();
        double mouseY = event.lattice$y();

        if (mouseButton == 0 && mouseY >= y && mouseY <= y + this.font.lineHeight) {
            int slashWidth = -1;

            int size = Math.min(this.categoryTitles.size(), this.categories.size());
            for (int i = 0; i < size; i++) {
                Component title = this.categoryTitles.get(i);

                int width = this.font.width(title);

                if (mouseX >= x && mouseX <= x+width) {
                    this.clickedCategoryPath = this.categories.subList(0, i+1);
                    return true;
                }

                if (i < this.categories.size()-1) {
                    x += width + 4;
                    if (slashWidth == -1) {
                        slashWidth = this.font.width(SLASH);
                    }
                    x += 4 + slashWidth;
                }
            }
        }

        return callSuper.getAsBoolean();
    }

    @Override
    public boolean lattice$mouseReleased(IMouseButtonEvent event, BooleanSupplier callSuper) {
        this.clickedCategoryPath = null;
        return callSuper.getAsBoolean();
    }

    @Override
    public @Nullable List<LatticeElements> switchToCategoryAfterClick() {
        return this.clickedCategoryPath;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

}
