package com.moulberry.lattice.widget;

import com.moulberry.lattice.multiversion.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public class WidgetWithText extends AbstractWidget implements IGuiEventListener {

    public final AbstractWidget widget;
    private final @Nullable Component title;
    private final @Nullable Component description;
    private final Font font;

    private int splitWithWidth = 0;
    private Language splitWithLanguage = null;
    private List<FormattedCharSequence> titleLines = List.of();
    private List<FormattedCharSequence> descriptionLines = List.of();

    public WidgetWithText(AbstractWidget widget, @Nullable Component title, @Nullable Component description, Font font) {
        super(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), widget.getMessage());
        this.widget = widget;
        this.title = title;
        this.description = description;
        this.font = font;
    }

    public List<FormattedCharSequence> getTitleLines() {
        ensureTitleAndDescriptionIsSplit();
        return this.titleLines;
    }

    public List<FormattedCharSequence> getDescriptionLines() {
        ensureTitleAndDescriptionIsSplit();
        return this.descriptionLines;
    }

    private void ensureTitleAndDescriptionIsSplit() {
        Language language = Language.getInstance();
        int width = Math.max(this.widget.getWidth() - 8, 80);

        if (this.descriptionLines == null || this.splitWithLanguage != language || this.splitWithWidth != width) {
            this.splitWithLanguage = language;
            this.splitWithWidth = width;
            if (this.title != null) {
                this.titleLines = this.font.split(this.title, width);
            }
            if (this.description != null) {
                this.descriptionLines = this.font.split(this.description, width);
            }
        }
    }

    private int getTitleHeight() {
        var lines = this.getTitleLines();
        if (lines.isEmpty()) {
            return 0;
        } else {
            return lines.size() * this.font.lineHeight;
        }
    }

    private int getDescriptionHeight() {
        var lines = this.getDescriptionLines();
        if (lines.isEmpty()) {
            return 0;
        } else {
            return 2 + lines.size() * this.font.lineHeight + 4;
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = this.getX() + 1;
        int y = this.getY();

        var titleLines = this.getTitleLines();

        if (!titleLines.isEmpty()) {
            for (FormattedCharSequence line : getTitleLines()) {
                LatticeMultiversion.drawString(guiGraphics, this.font, line, x, y, 0xFFFFFFFF);
                y += this.font.lineHeight;
            }
        }

        this.widget.render(guiGraphics, mouseX, mouseY, partialTick);

        x = this.getX()+4;
        y += this.widget.getHeight()+2;
        for (FormattedCharSequence line : getDescriptionLines()) {
            LatticeMultiversion.drawString(guiGraphics, this.font, line, x, y, 0xFFE0E0E0);
            y += this.font.lineHeight;
        }
    }

    @Override
    public boolean lattice$mouseClicked(IMouseButtonEvent event, BooleanSupplier callSuper) {
        int right = this.widget.getX() + this.widget.getWidth();
        int bottom = this.widget.getY() + this.widget.getHeight();
        double mouseX = event.lattice$x();
        double mouseY = event.lattice$y();
        if (mouseX >= this.widget.getX() && mouseY >= this.widget.getY() && mouseX <= right && mouseY <= bottom) {
            return event.lattice$passClickedTo(this.widget);
        }
        return false;
    }

    @Override
    public boolean lattice$mouseReleased(IMouseButtonEvent event, BooleanSupplier callSuper) {
        int right = this.widget.getX() + this.widget.getWidth();
        int bottom = this.widget.getY() + this.widget.getHeight();
        double mouseX = event.lattice$x();
        double mouseY = event.lattice$y();
        if (mouseX >= this.widget.getX() && mouseY >= this.widget.getY() && mouseX <= right && mouseY <= bottom) {
            return event.lattice$passReleasedTo(this.widget);
        }
        return false;
    }

    @Override
    public boolean lattice$mouseDragged(IMouseButtonEvent event, double dx, double dy, BooleanSupplier callSuper) {
        if (this.widget.isFocused()) {
            return event.lattice$passDraggedTo(this.widget, dx, dy);
        }
        return false;
    }

    // mouseScrolled implemented by MixinWidgetWithText

    @Override
    public boolean lattice$keyPressed(IKeyEvent event, BooleanSupplier callSuper) {
        if (this.widget.isFocused()) {
            return event.lattice$passPressedTo(this.widget);
        }
        return false;
    }

    @Override
    public boolean lattice$keyReleased(IKeyEvent event, BooleanSupplier callSuper) {
        if (this.widget.isFocused()) {
            return event.lattice$passReleasedTo(this.widget);
        }
        return false;
    }

    @Override
    public boolean lattice$charTyped(ICharacterEvent event, BooleanSupplier callSuper) {
        if (this.widget.isFocused()) {
            return event.lattice$passCharTypedTo(this.widget);
        }
        return false;
    }

    @Override
    public void setX(int x) {
        this.widget.setX(x);
    }

    @Override
    public void setY(int y) {
        this.widget.setY(y + this.getTitleHeight());
    }

    @Override
    public void setWidth(int width) {
        this.widget.setWidth(width);
    }

    @Override
    public int getX() {
        return this.widget.getX();
    }

    @Override
    public int getY() {
        return this.widget.getY() - this.getTitleHeight();
    }

    @Override
    public int getWidth() {
        return this.widget.getWidth();
    }

    @Override
    public int getHeight() {
        return this.widget.getHeight() + this.getTitleHeight() + this.getDescriptionHeight();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.widget.setFocused(focused);
    }

    @Override
    public boolean isFocused() {
        return this.widget.isFocused();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.widget.updateNarration(narrationElementOutput);
        if (this.description != null) {
            narrationElementOutput.add(NarratedElementType.HINT, this.description);
        }
    }

}
