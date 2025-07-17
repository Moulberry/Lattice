package com.moulberry.lattice.element;

import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.widget.WidgetWithText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class LatticeElement implements WidgetFunction {
    private final WidgetFunction widgetFunction;

    private Language lastLanguage = null;
    private String lastTitle = null;
    private String lastDescription = null;

    private final Component title;
    private final Component description;
    private boolean showTitleSeparately = false;
    private boolean disabled = false;

    public LatticeElement(WidgetFunction widgetFunction, @NotNull Component title, @Nullable Component description) {
        this.widgetFunction = widgetFunction;
        this.title = title;
        if (description == null) {
            this.description = null;
        } else if (description.getStyle().getColor() == null) {
            this.description = description.copy().withStyle(description.getStyle().withColor(0xFFE0E0E0));
        } else {
            this.description = description;
        }
    }

    @NotNull
    public Component title() {
        return this.title;
    }

    @Nullable
    public Component description() {
        return this.description;
    }

    public LatticeElement withShowTitleSeparately(boolean showTitleSeparately) {
        this.showTitleSeparately = showTitleSeparately;
        return this;
    }

    public LatticeElement withDisabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    @Override
    public @NotNull AbstractWidget createWidget(Font font, @NotNull Component title, @Nullable Component description, int width) {
        description = description == null ? this.description : description;

        boolean showTitleSeparately = this.showTitleSeparately;

        AbstractWidget widget;
        try {
            widget = this.createInnerWidget(font, title, description, width);
            if (this.disabled) {
                widget.active = false;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Component errorMsg = Component.literal("An unexpected error was encountered while trying to construct this widget. See logs for stacktrace").withStyle(ChatFormatting.RED);
            MultiLineTextWidget multiLineTextWidget = new MultiLineTextWidget(errorMsg, font);
            multiLineTextWidget.setMaxWidth(width);
            widget = multiLineTextWidget;
            showTitleSeparately = true;
        }

        if (description != null || showTitleSeparately) {
            return new WidgetWithText(widget, showTitleSeparately ? title : null, description, font);
        } else {
            return widget;
        }
    }

    @ApiStatus.Internal
    public AbstractWidget createInnerWidget(Font font, @NotNull Component title, @Nullable Component description, int width) {
        return this.widgetFunction.createWidget(font, title, description, width);
    }

    private void resolveComponentToString() {
        Language language = Language.getInstance();
        if (this.lastLanguage != language || this.lastTitle == null || (this.lastDescription == null && this.description != null)) {
            this.lastLanguage = language;
            this.lastTitle = this.title.getString().toLowerCase(Locale.ROOT);
            if (this.description != null) {
                this.lastDescription = this.description.getString().toLowerCase(Locale.ROOT);
            }
        }
    }

    public @Nullable String searchKeyPrimary() {
        this.resolveComponentToString();
        return this.lastTitle;
    }

    public @Nullable String searchKeySecondary() {
        this.resolveComponentToString();
        return this.lastDescription;
    }

}
