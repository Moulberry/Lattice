package com.moulberry.lattice.widget;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class CenteredStringWidget extends AbstractStringWidget {

    public CenteredStringWidget(int width, int height, Component component, Font font) {
        this(0, 0, width, height, component, font);
    }

    public CenteredStringWidget(int x, int y, int width, int height, Component component, Font font) {
        super(x, y, width, height, component, font);
        this.active = false;
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int widgetWidth = this.getWidth();
        int textWidth = font.width(component);

        FormattedCharSequence formattedCharSequence;
        if (textWidth > widgetWidth) {
            formattedCharSequence = this.clipText(component, widgetWidth);
            textWidth = widgetWidth;
        } else {
            formattedCharSequence = component.getVisualOrderText();
        }

        int x = this.getX() + (widgetWidth - textWidth) / 2;
        int y = this.getY() + (this.getHeight() - font.lineHeight) / 2;
        LatticeMultiversion.drawString(guiGraphics, font, formattedCharSequence, x, y, this.getColor());
    }

    private FormattedCharSequence clipText(Component component, int i) {
        Font font = this.getFont();
        FormattedText formattedText = font.substrByWidth(component, i - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(new FormattedText[]{formattedText, CommonComponents.ELLIPSIS}));
    }
}
