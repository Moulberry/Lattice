package com.moulberry.lattice.element;

import com.moulberry.lattice.LatticeDynamicFrequency;
import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.widget.WidgetWithText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class LatticeElement {
    private final WidgetFunction widgetFunction;

    private Language lastLanguage = null;
    private String lastTitle = null;
    private String lastDescription = null;

    private final Component title;
    private final Component description;
    private boolean showTitleSeparately = false;
    private boolean canBeSearched = true;

    private LatticeDynamicCondition disabledDynamic = null;
    private LatticeDynamicCondition hiddenDynamic = null;

    public LatticeElement(WidgetFunction widgetFunction, @NotNull Component title, @Nullable Component description) {
        this.widgetFunction = widgetFunction;
        this.title = title;
        this.description = description;
    }

    @NotNull
    public Component title() {
        return this.title;
    }

    @Nullable
    public Component description() {
        return this.description;
    }

    public void showTitleSeparately(boolean showTitleSeparately) {
        this.showTitleSeparately = showTitleSeparately;
    }

    public void canBeSearched(boolean canBeSearched) {
        this.canBeSearched = canBeSearched;
    }

    public void disabledDynamic(LatticeDynamicCondition disabledDynamic) {
        this.disabledDynamic = disabledDynamic;
    }

    public void hiddenDynamic(LatticeDynamicCondition hiddenDynamic) {
        this.hiddenDynamic = hiddenDynamic;
    }

    public boolean showTitleSeparately() {
        return this.showTitleSeparately;
    }

    public boolean canBeSearched() {
        return canBeSearched;
    }

    public LatticeDynamicCondition disabledDynamic() {
        return this.disabledDynamic;
    }

    public LatticeDynamicCondition hiddenDynamic() {
        return this.hiddenDynamic;
    }

    @ApiStatus.Internal
    public @Nullable AbstractWidget createInnerWidget(Font font, @NotNull Component title, @Nullable Component description, int width) {
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
