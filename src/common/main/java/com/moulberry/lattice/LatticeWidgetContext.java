package com.moulberry.lattice;

import com.moulberry.lattice.element.LatticeDynamicCondition;
import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.widget.WidgetWithText;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public class LatticeWidgetContext {

    private enum WidgetDisableOrHide {
        NONE,
        DISABLED,
        HIDDEN
    }

    private Font font;
    private int width;

    private final Map<LatticeElement, WidgetDisableOrHide> disableOrHideMap = new HashMap<>();
    private final Object2BooleanOpenHashMap<BooleanSupplier> memorizedConditions = new Object2BooleanOpenHashMap<>();
    private final Map<LatticeElement, AbstractWidget> widgetsWithConditions = new HashMap<>();
    private final Map<AbstractWidget, BooleanSupplier> hideOnTick = new HashMap<>();
    private final Map<AbstractWidget, BooleanSupplier> disableOnTick = new HashMap<>();

    public LatticeWidgetContext(Font font, int width) {
        this.font = font;
        this.width = width;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean hasAnyOnTickConditions() {
        return !this.hideOnTick.isEmpty() || !this.disableOnTick.isEmpty();
    }

    public boolean checkMemorizedCondition(BooleanSupplier booleanSupplier) {
        return this.memorizedConditions.computeIfAbsent(booleanSupplier, supplier -> ((BooleanSupplier) supplier).getAsBoolean());
    }

    public boolean tickAndGetIsHidden(AbstractWidget widget) {
        BooleanSupplier hideCondition = this.hideOnTick.get(widget);
        if (hideCondition != null && checkMemorizedCondition(hideCondition)) {
            return true;
        }

        BooleanSupplier disableCondition = this.disableOnTick.get(widget);
        if (disableCondition != null) {
            widget.active = !checkMemorizedCondition(disableCondition);
            if (widget instanceof WidgetWithText widgetWithText) {
                widgetWithText.widget.active = widget.active;
            }
        }

        return false;
    }

    public void finishTick() {
        this.memorizedConditions.clear();
    }

    public @Nullable AbstractWidget create(LatticeElement element) {
        return this.create(element, null, null, 0);
    }

    public @Nullable AbstractWidget create(LatticeElement element, @Nullable Component overrideTitle, @Nullable Component overrideDescription, int overrideWidth) {
        WidgetDisableOrHide disableOrHide = this.disableOrHideMap.computeIfAbsent(element, LatticeWidgetContext::checkElementDisabledOrHidden);
        if (disableOrHide == WidgetDisableOrHide.HIDDEN) {
            return null;
        }

        AbstractWidget oldWidget = this.widgetsWithConditions.remove(element);
        if (oldWidget != null) {
            this.hideOnTick.remove(oldWidget);
            this.disableOnTick.remove(oldWidget);
        }

        Component title = overrideTitle == null ? element.title() : overrideTitle;
        Component description = overrideDescription == null ? element.description() : overrideDescription;
        int width = overrideWidth <= 0 ? this.width : overrideWidth;

        boolean showTitleSeparately = element.showTitleSeparately();

        // Check if hidden, and return null if so
        var hiddenDynamic = element.hiddenDynamic();
        if (hiddenDynamic != null && hiddenDynamic.frequency() == LatticeDynamicFrequency.WHEN_CATEGORY_OPENED && hiddenDynamic.condition().getAsBoolean()) {
            return null;
        }

        AbstractWidget widget;
        try {
            widget = element.createInnerWidget(font, title, description, width);
            if (widget == null) {
                return null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Component errorMsg = Component.literal("An unexpected error was encountered while trying to construct this widget. See logs for stacktrace").withStyle(ChatFormatting.RED);
            MultiLineTextWidget multiLineTextWidget = new MultiLineTextWidget(errorMsg, font);
            multiLineTextWidget.setMaxWidth(width);
            widget = multiLineTextWidget;
            showTitleSeparately = true;
        }

        var disabledDynamic = element.disabledDynamic();

        if (disableOrHide == WidgetDisableOrHide.DISABLED) {
            widget.active = false;
        } else if (disabledDynamic != null && disabledDynamic.condition().getAsBoolean()) {
            widget.active = false;
        }

        if (description != null || showTitleSeparately) {
            widget = new WidgetWithText(widget, showTitleSeparately ? title : null, description, font);
        }

        boolean checkHiddenEveryTick = hiddenDynamic != null && hiddenDynamic.frequency() == LatticeDynamicFrequency.EVERY_TICK;
        boolean checkDisabledEveryTick = disabledDynamic != null && disabledDynamic.frequency() == LatticeDynamicFrequency.EVERY_TICK;
        if (checkHiddenEveryTick || checkDisabledEveryTick) {
            this.widgetsWithConditions.put(element, widget);
            if (checkHiddenEveryTick) {
                this.hideOnTick.put(widget, hiddenDynamic.condition());
            }
            if (checkDisabledEveryTick) {
                this.disableOnTick.put(widget, disabledDynamic.condition());
            }
        }

        return widget;
    }

    private static WidgetDisableOrHide checkElementDisabledOrHidden(LatticeElement element) {
       if (checkDynamicCondition(element.hiddenDynamic())) {
           return WidgetDisableOrHide.HIDDEN;
       } else if (checkDynamicCondition(element.disabledDynamic())) {
           return WidgetDisableOrHide.DISABLED;
       } else {
           return WidgetDisableOrHide.NONE;
       }
    }

    private static boolean checkDynamicCondition(LatticeDynamicCondition condition) {
        if (condition != null && condition.frequency() == LatticeDynamicFrequency.ONCE) {
            return condition.condition().getAsBoolean();
        } else {
            return false;
        }
    }

}
