package com.moulberry.lattice.widget;

import com.moulberry.lattice.LatticeConfigScreen;
import com.moulberry.lattice.LatticeTextComponents;
import com.moulberry.lattice.LatticeWidgetContext;
import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.element.LatticeElements;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class SubcategoryButton extends Button implements WidgetExtraFunctionality {
    private final Font font;
    private final int baseWidth;
    private final int index;
    private final LatticeElements subcategory;
    private final Set<LatticeElements> openedSubcategories;
    private final LatticeWidgetContext widgetContext;
    private final Component closedTitle;
    private final Component openedTitle;
    private final List<AbstractWidget> childWidgets = new ArrayList<>();
    private final int widthForChildren;

    public SubcategoryButton(Font font, int baseWidth, int index, LatticeElements subcategory, Set<LatticeElements> openedSubcategories,
            LatticeWidgetContext widgetContext, Component closedTitle, Component openedTitle) {
        super(0, 0, calculateWidth(baseWidth, index), 20, closedTitle, button -> ((SubcategoryButton)button).handlePress(), Supplier::get);
        this.font = font;
        this.baseWidth = baseWidth;
        this.index = index;
        this.subcategory = subcategory;
        this.openedSubcategories = openedSubcategories;
        this.widgetContext = widgetContext;
        this.closedTitle = closedTitle;
        this.openedTitle = openedTitle;
        this.widthForChildren = calculateWidth(baseWidth, index+1);

        if (openedSubcategories.contains(subcategory)) {
            this.setMessage(openedTitle);
            this.createWidgets();
        }
    }

    public LatticeElements getSubcategory() {
        return subcategory;
    }

    private static int calculateWidth(int baseWidth, int index) {
        if (index <= 0) {
            return baseWidth;
        }
        int startFalloffAt = 4;
        int normalDistance = 10;
        if (index <= startFalloffAt) {
            return baseWidth - 10 * index;
        } else {
            double factor = startFalloffAt * normalDistance / (double) baseWidth;
            double falloff = 1.0 - 1.0 / (1.0 + (index/(double)startFalloffAt)*(index/(double)startFalloffAt));
            return baseWidth - (int)(baseWidth * falloff * 2 * factor);
        }
    }

    private void createWidgets() {
        this.childWidgets.clear();

        for (LatticeElement option : this.subcategory.options) {
            var widget = this.widgetContext.create(option, null, null, this.widthForChildren);
            if (widget != null) {
                this.childWidgets.add(widget);
            }
        }

        for (LatticeElements subcategory : this.subcategory.subcategories) {
            if (subcategory.isEmpty()) {
                continue;
            }

            var subcategoryButton = this.widgetContext.createSubcategory(subcategory, this.index+1);
            if (subcategoryButton != null) {
                this.childWidgets.add(subcategoryButton);
            }
        }
    }

    public void handlePress() {
        if (this.openedSubcategories.add(subcategory)) {
            this.setMessage(this.openedTitle);
            this.createWidgets();
        } else {
            this.openedSubcategories.remove(subcategory);
            this.setMessage(this.closedTitle);
            this.childWidgets.clear();
        }
    }

    public void setOpen(boolean open) {
        if (open) {
            if (this.openedSubcategories.add(subcategory)) {
                this.setMessage(this.openedTitle);
                this.createWidgets();
            }
        } else {
            if (this.openedSubcategories.remove(subcategory)) {
                this.setMessage(this.closedTitle);
                this.childWidgets.clear();
            }
        }
    }

    @Override
    public List<AbstractWidget> extraWidgets() {
        return this.childWidgets;
    }

    @Override
    public int extraWidgetHorizonalOffset() {
        return this.getWidth() - this.widthForChildren;
    }

    @Override
    public int renderVerticalLineForExtraWidgetsAtX() {
        return this.getX() + this.extraWidgetHorizonalOffset()/2;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
    }
}
