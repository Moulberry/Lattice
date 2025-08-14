package com.moulberry.lattice;

import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.element.LatticeElements;
import com.moulberry.lattice.keybind.LatticeInputType;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.lattice.widget.SubcategoryButton;
import com.moulberry.lattice.widget.WidgetWithText;
import com.moulberry.lattice.widget.WidgetExtraFunctionality;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@ApiStatus.Internal
public class LatticeConfigScreen extends Screen {

    private static final int SCROLL_BAR_WIDTH = 6;

    private static final int TOP_PADDING = 48;
    private static final int BOTTOM_PADDING = 30;
    public static final int ITEM_PADDING = 4;

    private int buttonWidth = 160;

    private final LatticeElements rootCategory;
    private LatticeElements activeCategory;

    @Nullable
    private final Runnable onClosed;
    @Nullable
    private final Screen closeTo;

    private final Map<LatticeElements, AbstractButton> buttonForCategory = new LinkedHashMap<>();
    private final List<AbstractButton> categoryButtons = new ArrayList<>();
    private final List<AbstractWidget> optionEntries = new ArrayList<>();

    private final Set<LatticeElements> openedSubcategories = new HashSet<>();

    private int categoryContentHeight = 0;
    private double categoryScrollAmount = 0;
    private int optionContentHeight = 0;
    private double optionScrollAmount = 0;
    private int searchContentHeight = 0;
    private double searchScrollAmount = 0;

    private LatticeElements scrollToSubcategory = null;
    private int scrollToSubcategoryStart = -1;
    private int scrollToSubcategoryEnd = -1;

    private WidgetExtraFunctionality currentExtraFunctionalityWidget = null;
    private boolean scrollingCategoryList = false;
    private boolean scrollingOptionList = false;
    private boolean scrollingSearchList = false;

    private boolean suppressInputThisTick = false;

    private EditBox searchBox = null;
    private boolean searching = false;
    private final LatticeElementSearcher elementSearcher;
    private final LatticeWidgetContext widgetContext;

    private boolean tickWidgetsOnPosition = false;

    public LatticeConfigScreen(LatticeElements options, @Nullable Runnable onClosed, @Nullable Screen closeTo) {
        super(options.title == null ? LatticeTextComponents.DEFAULT_CONFIG_NAME : options.title);
        this.rootCategory = options;

        if (!options.options.isEmpty()) {
            this.activeCategory = options;
        } else if (!options.subcategories.isEmpty()) {
            this.activeCategory = options.subcategories.get(0);
        }

        this.widgetContext = new LatticeWidgetContext(this.font, this.buttonWidth, this.openedSubcategories);
        this.elementSearcher = new LatticeElementSearcher(options, this.widgetContext);
        this.onClosed = onClosed;
        this.closeTo = closeTo;
    }

    @Override
    protected void init() {
        if (this.width <= 280) {
            this.buttonWidth = 140;
        } else if (this.width <= 560) {
            this.buttonWidth = this.width/2;
        } else {
            this.buttonWidth = 140 * (1+this.width/560);
        }

        this.buttonWidth -= ITEM_PADDING*2; // padding
        this.buttonWidth -= SCROLL_BAR_WIDTH + ITEM_PADDING; // scrollbar

        this.widgetContext.setFont(this.font);
        this.widgetContext.setWidth(this.buttonWidth);

        this.categoryContentHeight = 0;
        this.optionContentHeight = 0;
        this.searchContentHeight = 0;

        this.scrollingCategoryList = false;
        this.scrollingOptionList = false;
        this.scrollingSearchList = false;

        // Search box
        this.searchBox = new EditBox(this.font, this.width/2 - 100, 22, 200, 20, this.searchBox, CommonComponents.EMPTY);
        this.searchBox.setResponder(searchString -> {
            if (searchString.isBlank() && !this.searching) {
                return;
            }

            this.currentExtraFunctionalityWidget = null;
            this.scrollingCategoryList = false;
            this.scrollingOptionList = false;
            this.scrollingSearchList = false;
            if (searchString.isBlank()) {
                this.setCategory(this.activeCategory);
            } else {
                this.searching = true;
                this.elementSearcher.search(searchString);
                this.resetTickWidgetsOnPosition();
                this.positionSearchWidgets();
                this.setScrollToSubcategory(null);
            }
        });
        this.addRenderableWidget(this.searchBox);

        // Done button
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            this.onClose();
        }).bounds(this.width/2 - 100, this.height - (BOTTOM_PADDING-2)/2 - 20/2, 200, 20).build());

        if (this.searching) {
            this.positionSearchWidgets();
        }

        // Category buttons
        this.categoryButtons.clear();
        this.buttonForCategory.clear();
        if (!this.rootCategory.options.isEmpty()) {
            var button = Button.builder(LatticeTextComponents.ROOT_CATEGORY_NAME, btn -> {
                this.optionScrollAmount = 0;
                this.setCategory(this.rootCategory);
            }).size(this.buttonWidth, 20).build();
            this.categoryButtons.add(button);
            this.buttonForCategory.put(this.rootCategory, button);
        }

        for (LatticeElements subcategory : this.rootCategory.subcategories) {
            var button = Button.builder(subcategory.getTitleOrDefault(), btn -> {
                this.optionScrollAmount = 0;
                this.setCategory(subcategory);
            }).size(this.buttonWidth, 20).build();
            this.categoryButtons.add(button);
            this.buttonForCategory.put(subcategory, button);
        }
        this.positionCategoryWidgets();

        // Option widgets
        setCategory(this.activeCategory);
    }

    private void setCategory(LatticeElements category) {
        this.searching = false;
        this.searchContentHeight = 0;
        this.searchScrollAmount = 0;
        this.currentExtraFunctionalityWidget = null;
        this.optionEntries.clear();
        this.activeCategory = category;
        this.setScrollToSubcategory(null);

        for (AbstractButton button : this.categoryButtons) {
            button.active = true;
        }

        if (this.activeCategory == null) {
            this.positionOptionWidgets();
            return;
        }

        AbstractButton buttonForConfig = this.buttonForCategory.get(category);
        if (buttonForConfig != null) {
            buttonForConfig.active = false;
        }

        // Create options
        for (LatticeElement option : this.activeCategory.options) {
            var widget = this.widgetContext.create(option);
            if (widget != null) {
                this.optionEntries.add(widget);
            }
        }

        if (this.activeCategory != this.rootCategory) {
            for (LatticeElements subcategory : this.activeCategory.subcategories) {
                if (subcategory.isEmpty()) {
                    continue;
                }

                var subcategoryButton = this.widgetContext.createSubcategory(subcategory, 0);
                if (subcategoryButton != null) {
                    this.optionEntries.add(subcategoryButton);
                }
            }
        }

        this.resetTickWidgetsOnPosition();
        this.positionOptionWidgets();
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        List<GuiEventListener> children = new ArrayList<>(super.children());
        if (this.searching) {
            if (this.tickWidgetsOnPosition) {
                this.positionSearchWidgets();
            }
            var widgets = this.elementSearcher.getSearchedWidgets(this.font, this.buttonWidth);
            if (widgets != null) {
                for (AbstractWidget widget : widgets) {
                    if (widget.visible) {
                        children.add(widget);
                    }
                }
            }
        } else {
            if (this.tickWidgetsOnPosition) {
                this.positionOptionWidgets();
            }
            children.addAll(this.categoryButtons);
            addVisibleWidgetsRecursive(children, this.optionEntries);
        }

        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            children.add(0, popupWidget);
        }

        return children;
    }

    private void addVisibleWidgetsRecursive(List<GuiEventListener> list, List<AbstractWidget> widgets) {
        if (widgets == null) {
            return;
        }
        for (AbstractWidget widget : widgets) {
            if (!widget.visible) {
                continue;
            }
            list.add(widget);
            if (widget instanceof WidgetExtraFunctionality extraFunctionality) {
                addVisibleWidgetsRecursive(list, extraFunctionality.extraWidgets());
            }
        }
    }

    @Override
    public @NotNull Optional<GuiEventListener> getChildAt(double mouseX, double mouseY) {
        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            if (popupWidget.isMouseOver(mouseX, mouseY)) {
                return Optional.of(popupWidget);
            }
            return Optional.empty();
        }

        Optional<GuiEventListener> child = super.getChildAt(mouseX, mouseY);
        if (child.isPresent()) {
            return child;
        }

        if (mouseY >= TOP_PADDING && mouseY <= this.height-BOTTOM_PADDING) {
            if (this.searching) {
                if (this.tickWidgetsOnPosition) {
                    this.positionSearchWidgets();
                }
                var widgets = this.elementSearcher.getSearchedWidgets(this.font, this.buttonWidth);
                if (widgets != null) {
                    for (AbstractWidget widget : widgets) {
                        if (!widget.visible) {
                            continue;
                        }
                        if (widget.isMouseOver(mouseX, mouseY)) {
                            return Optional.of(widget);
                        }
                    }
                }
                return Optional.empty();
            } else if (mouseX < this.width/2f) {
                for (AbstractButton categoryButton : this.categoryButtons) {
                    if (categoryButton.isMouseOver(mouseX, mouseY)) {
                        return Optional.of(categoryButton);
                    }
                }
                return Optional.empty();
            } else {
                if (this.tickWidgetsOnPosition) {
                    this.positionOptionWidgets();
                }
                return checkMouseOverRecursive(this.optionEntries, mouseX, mouseY);
            }
        }

        return Optional.empty();
    }

    private Optional<GuiEventListener> checkMouseOverRecursive(List<AbstractWidget> widgets, double mouseX, double mouseY) {
        if (widgets == null) {
            return Optional.empty();
        }
        for (AbstractWidget abstractWidget : widgets) {
            if (!abstractWidget.visible) {
                continue;
            }
            if (abstractWidget.isMouseOver(mouseX, mouseY)) {
                return Optional.of(abstractWidget);
            }
            if (abstractWidget instanceof WidgetExtraFunctionality extraFunctionality) {
                var result = checkMouseOverRecursive(extraFunctionality.extraWidgets(), mouseX, mouseY);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    private boolean shouldSuppressInput() {
        return this.suppressInputThisTick || (this.currentExtraFunctionalityWidget != null && this.currentExtraFunctionalityWidget.listeningForRawKeyInput());
    }

    // clearFocus() is private in older versions, so we just reimplement it
    private void clearFocusInternal() {
        ComponentPath path = this.getCurrentFocusPath();
        if (path != null) {
            path.applyFocus(false);
        }
        this.currentExtraFunctionalityWidget = null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.setScrollToSubcategory(null);

        if (this.currentExtraFunctionalityWidget != null && this.currentExtraFunctionalityWidget.listeningForRawKeyInput()) {
            sendRawInputToWidget(LatticeInputType.MOUSE, mouseButton, false);
            return true;
        }

        if (this.shouldSuppressInput()) {
            return false;
        }

        if (mouseY >= TOP_PADDING && mouseY <= this.height-BOTTOM_PADDING) {
            if (this.searching) {
                int searchScrollBarX = getSearchListScrollBarX();
                if (mouseX >= searchScrollBarX && mouseX <= searchScrollBarX+SCROLL_BAR_WIDTH) {
                    this.scrollingSearchList = true;
                    return true;
                }
            } else {
                int categoryScrollBarX = getCategoryListScrollBarX();
                if (mouseX >= categoryScrollBarX && mouseX <= categoryScrollBarX+SCROLL_BAR_WIDTH) {
                    this.scrollingCategoryList = true;
                    return true;
                }

                int optionScrollBarX = getOptionListScrollBarX();
                if (mouseX >= optionScrollBarX && mouseX <= optionScrollBarX+SCROLL_BAR_WIDTH) {
                    this.scrollingOptionList = true;
                    return true;
                }
            }
        }

        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            var newExtraFunctionalityWidget = this.getExtraFunctionalityWidget();
            if (newExtraFunctionalityWidget != null) {
                List<LatticeElements> switchToCategoryPath = newExtraFunctionalityWidget.switchToCategoryAfterClick();
                if (switchToCategoryPath != null && !switchToCategoryPath.isEmpty()) {
                    for (int i = 1; i < switchToCategoryPath.size(); i++) {
                        this.openedSubcategories.add(switchToCategoryPath.get(i));
                    }
                    this.setCategory(switchToCategoryPath.get(0));
                    if (switchToCategoryPath.size() > 1) {
                        this.setScrollToSubcategory(switchToCategoryPath.get(switchToCategoryPath.size() - 1));
                    }
                    return true;
                }

                this.currentExtraFunctionalityWidget = newExtraFunctionalityWidget;
            }
            return true;
        }

        this.clearFocusInternal();
        return false;
    }

    private void sendRawInputToWidget(LatticeInputType inputType, int value, boolean release) {
        if (this.currentExtraFunctionalityWidget.handleRawInput(inputType, value, release)) {
            if (this.searching) {
                callAfterRawInputHandledByAnyRecursive(this.elementSearcher.getSearchedWidgets(this.font, this.buttonWidth));
            } else {
                callAfterRawInputHandledByAnyRecursive(this.optionEntries);
            }
        }

        if (!this.currentExtraFunctionalityWidget.listeningForRawKeyInput()) {
            this.clearFocusInternal();
        }
        this.suppressInputThisTick = true;
    }

    private static void callAfterRawInputHandledByAnyRecursive(List<AbstractWidget> widgets) {
        if (widgets == null) {
            return;
        }
        for (AbstractWidget widget : widgets) {
            if (widget instanceof WidgetExtraFunctionality widgetExtraFunctionality) {
                widgetExtraFunctionality.afterRawInputHandledByAny();
                callAfterRawInputHandledByAnyRecursive(widgetExtraFunctionality.extraWidgets());
            } else if (widget instanceof WidgetWithText withDescription) {
                if (withDescription.widget instanceof WidgetExtraFunctionality widgetExtraFunctionality) {
                    widgetExtraFunctionality.afterRawInputHandledByAny();
                }
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        if (this.currentExtraFunctionalityWidget != null && this.currentExtraFunctionalityWidget.listeningForRawKeyInput()) {
            sendRawInputToWidget(LatticeInputType.MOUSE, mouseButton, true);
            return true;
        }

        if (this.scrollingCategoryList) {
            this.scrollingCategoryList = false;
            return true;
        }
        if (this.scrollingOptionList) {
            this.scrollingOptionList = false;
            return true;
        }
        if (this.scrollingSearchList) {
            this.scrollingSearchList = false;
            return true;
        }

        if (this.shouldSuppressInput()) {
            return false;
        }

        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        if (this.scrollingCategoryList) {
            this.categoryScrollAmount = calculateScrollFromDrag(dragY, this.categoryContentHeight, this.categoryScrollAmount);
        }
        if (this.scrollingOptionList) {
            this.optionScrollAmount = calculateScrollFromDrag(dragY, this.optionContentHeight, this.optionScrollAmount);
        }
        if (this.scrollingSearchList) {
            this.searchScrollAmount = calculateScrollFromDrag(dragY, this.searchContentHeight, this.searchScrollAmount);
        }

        return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
    }

    private double calculateScrollFromDrag(double dragY, int contentHeight, double scrollAmount) {
        int maxScroll = this.maxScroll(contentHeight);
        if (maxScroll > 0) {
            int scrollBarHeight = this.scrollableAreaHeight();
            int scrollerHeight = Math.max(32, scrollBarHeight * scrollBarHeight / contentHeight);

            double dragAmount = dragY / (scrollBarHeight - scrollerHeight);
            return Math.max(0, Math.min(maxScroll, scrollAmount + dragAmount * maxScroll));
        } else {
            return 0;
        }
    }

    // mouseScrolled implemented by MixinLatticeConfigScreen

    public boolean mouseScrolledInternal(double mouseX, double mouseY, double scrollY) {
        this.setScrollToSubcategory(null);

        if (this.shouldSuppressInput()) {
            return true;
        }

        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            if (popupWidget.isMouseOver(mouseX, mouseY)) {
                return LatticeMultiversion.callMouseScrolled(popupWidget, mouseX, mouseY, scrollY);
            }
            return true;
        }

        if (mouseY >= TOP_PADDING && mouseY <= this.height-BOTTOM_PADDING) {
            if (this.searching) {
                int maxScroll = this.maxScroll(this.searchContentHeight);
                if (maxScroll > 0) {
                    this.searchScrollAmount = Math.max(0, Math.min(maxScroll, this.searchScrollAmount - scrollY * 16));
                    return true;
                }
            } else if (mouseX < this.width / 2f) {
                int maxScroll = this.maxScroll(this.categoryContentHeight);
                if (maxScroll > 0) {
                    this.categoryScrollAmount = Math.max(0, Math.min(maxScroll, this.categoryScrollAmount - scrollY * 16));
                    return true;
                }
            } else {
                int maxScroll = this.maxScroll(this.optionContentHeight);
                if (maxScroll > 0) {
                    this.optionScrollAmount = Math.max(0, Math.min(maxScroll, this.optionScrollAmount - scrollY * 16));
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.suppressInputThisTick = false;
        this.resetTickWidgetsOnPosition();
    }

    private void resetTickWidgetsOnPosition() {
        if (!this.tickWidgetsOnPosition && this.widgetContext.hasAnyOnTickConditions()) {
            this.tickWidgetsOnPosition = true;
        }
    }

    @Override
    public boolean keyPressed(int keysym, int scancode, int mods) {
        if (this.currentExtraFunctionalityWidget != null && this.currentExtraFunctionalityWidget.listeningForRawKeyInput()) {
            if (keysym != GLFW.GLFW_KEY_UNKNOWN) {
                sendRawInputToWidget(LatticeInputType.KEYSYM, keysym, false);
            } else {
                sendRawInputToWidget(LatticeInputType.SCANCODE, scancode, false);
            }
            return true;
        }

        if (this.shouldSuppressInput()) {
            return false;
        }

        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            if (keysym == GLFW.GLFW_KEY_ESCAPE) {
                this.clearFocusInternal();
                return true;
            }
            if (popupWidget.keyPressed(keysym, scancode, mods)) {
                return true;
            }
            if (popupWidget instanceof ContainerEventHandler containerWidget) {
                FocusNavigationEvent focusNavigationEvent = switch (keysym) {
                    case GLFW.GLFW_KEY_TAB -> new FocusNavigationEvent.TabNavigation(!Screen.hasShiftDown());
                    case GLFW.GLFW_KEY_RIGHT -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.RIGHT);
                    case GLFW.GLFW_KEY_LEFT -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.LEFT);
                    case GLFW.GLFW_KEY_DOWN -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.DOWN);
                    case GLFW.GLFW_KEY_UP -> new FocusNavigationEvent.ArrowNavigation(ScreenDirection.UP);
                    default -> null;
                };
                if (focusNavigationEvent != null) {
                    ComponentPath componentPath = containerWidget.nextFocusPath(focusNavigationEvent);
                    if (componentPath == null && focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
                        var currentFocus = containerWidget.getCurrentFocusPath();
                        if (currentFocus != null) {
                            currentFocus.applyFocus(false);
                        }
                        componentPath = containerWidget.nextFocusPath(focusNavigationEvent);
                    }
                    if (componentPath != null) {
                        var currentFocus = containerWidget.getCurrentFocusPath();
                        if (currentFocus != null) {
                            currentFocus.applyFocus(false);
                        }
                        componentPath.applyFocus(true);
                    }
                }
                return false;
            }
        }

        if (keysym == GLFW.GLFW_KEY_ESCAPE && this.searching) {
            this.searchBox.setValue("");
            this.setFocused(this.searchBox);
            return true;
        }

        if (super.keyPressed(keysym, scancode, mods)) {
            return true;
        }

        int modifier = Minecraft.ON_OSX ? GLFW.GLFW_MOD_SUPER : GLFW.GLFW_MOD_CONTROL;
        if (keysym == GLFW.GLFW_KEY_F && (mods & modifier) != 0) {
            this.setFocused(this.searchBox);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(int keysym, int scancode, int modifiers) {
        if (this.currentExtraFunctionalityWidget != null && this.currentExtraFunctionalityWidget.listeningForRawKeyInput()) {
            if (keysym != GLFW.GLFW_KEY_UNKNOWN) {
                sendRawInputToWidget(LatticeInputType.KEYSYM, keysym, true);
            } else {
                sendRawInputToWidget(LatticeInputType.SCANCODE, scancode, true);
            }
            return true;
        }

        if (this.shouldSuppressInput()) {
            return false;
        }

        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            return popupWidget.keyReleased(keysym, scancode, modifiers);
        }

        return super.keyReleased(keysym, scancode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int i) {
        if (this.shouldSuppressInput()) {
            return false;
        }

        GuiEventListener popupWidget = this.getPopup();
        if (popupWidget != null) {
            return popupWidget.charTyped(c, i);
        }

        return super.charTyped(c, i);
    }

    private void renderDividersAndBackground(GuiGraphics guiGraphics) {
        if (this.searching) {
            // Header
            guiGraphics.fill(0, TOP_PADDING-2, this.width, TOP_PADDING-1, 0x33FFFFFF);
            // Footer
            guiGraphics.fill(0, this.height-BOTTOM_PADDING+1, this.width, this.height-BOTTOM_PADDING+2, 0x33FFFFFF);

            // Header
            guiGraphics.fill(0, TOP_PADDING-1, this.width, TOP_PADDING, 0xBF000000);
            // Footer
            guiGraphics.fill(0, this.height-BOTTOM_PADDING, this.width, this.height-BOTTOM_PADDING+1, 0xBF000000);

            // Background
            guiGraphics.fill(0, TOP_PADDING, this.width, this.height-BOTTOM_PADDING, 0x70000000);
        } else {
            int mid = (this.width-1)/2;
            // Header
            guiGraphics.fill(0, TOP_PADDING-2, this.width, TOP_PADDING-1, 0x33FFFFFF);
            // Vertical separator
            guiGraphics.fill(mid, TOP_PADDING-1, mid+1, this.height-BOTTOM_PADDING+1, 0x33FFFFFF);
            // Footer
            guiGraphics.fill(0, this.height-BOTTOM_PADDING+1, this.width, this.height-BOTTOM_PADDING+2, 0x33FFFFFF);

            // Header
            guiGraphics.fill(0, TOP_PADDING-1, mid, TOP_PADDING, 0xBF000000);
            guiGraphics.fill(mid+1, TOP_PADDING-1, this.width, TOP_PADDING, 0xBF000000);
            // Vertical separator
            guiGraphics.fill(mid+1, TOP_PADDING, mid+2, this.height-BOTTOM_PADDING, 0xBF000000);
            // Footer
            guiGraphics.fill(0, this.height-BOTTOM_PADDING, mid, this.height-BOTTOM_PADDING+1, 0xBF000000);
            guiGraphics.fill(mid+1, this.height-BOTTOM_PADDING, this.width, this.height-BOTTOM_PADDING+1, 0xBF000000);

            // Background (left)
            guiGraphics.fill(0, TOP_PADDING, mid, this.height-BOTTOM_PADDING, 0x70000000);
            // Background (right)
            guiGraphics.fill(mid+2, TOP_PADDING, this.width, this.height-BOTTOM_PADDING, 0x70000000);
        }

        guiGraphics.drawCenteredString(this.font, this.title, this.width/2, 8, 0xFFFFFFFF);
    }

    /* 1.20.1 to 1.21.5:
    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        this.renderDividersAndBackground(guiGraphics);
    }*/

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1.21.6+: this.renderDividersAndBackground(guiGraphics);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        WidgetExtraFunctionality popupHolder = this.currentExtraFunctionalityWidget;
        GuiEventListener popupWidget = popupHolder == null ? null : popupHolder.getPopup();

        int mouseXForNonPopup = popupWidget == null ? mouseX : 0;
        int mouseYForNonPopup = popupWidget == null ? mouseY : 0;

        if (this.searching) {
            guiGraphics.enableScissor(0, TOP_PADDING, this.width, this.height-BOTTOM_PADDING);
            positionAndRenderSearchWidgets(guiGraphics, mouseXForNonPopup, mouseYForNonPopup, partialTick);
            guiGraphics.disableScissor();

            renderSearchListScrollBar(guiGraphics);
        } else {
            int mid = (this.width-1)/2;

            guiGraphics.enableScissor(0, TOP_PADDING, mid, this.height-BOTTOM_PADDING);
            positionAndRenderCategoryWidgets(guiGraphics, mouseXForNonPopup, mouseYForNonPopup, partialTick);
            guiGraphics.disableScissor();

            this.scrollToSubcategoryStart = -1;
            this.scrollToSubcategoryEnd = -1;

            guiGraphics.enableScissor(mid+2, TOP_PADDING, this.width, this.height-BOTTOM_PADDING);
            positionAndRenderOptionWidgets(guiGraphics, mouseXForNonPopup, mouseYForNonPopup, partialTick);
            guiGraphics.disableScissor();

            if (this.scrollToSubcategory == null || this.scrollToSubcategoryStart < 0 || this.scrollToSubcategoryEnd < 0) {
                this.scrollToSubcategory = null;
            } else {
                int maxScroll = this.maxScroll(this.optionContentHeight);
                int scrollTop = Math.max(this.scrollToSubcategoryStart, 0);
                int scrollBottom = Math.min(this.scrollToSubcategoryEnd - this.scrollableAreaHeight(), maxScroll);

                if (this.optionScrollAmount > scrollTop) {
                    // Scroll up
                    double factor = Math.max(0.001D, Math.min(1.0D, 1.0D - Math.pow(0.85D, partialTick)));
                    this.optionScrollAmount -= 4 * partialTick + (this.optionScrollAmount - scrollTop) * factor;
                    if (this.optionScrollAmount <= scrollTop) {
                        this.optionScrollAmount = scrollTop;
                        this.setScrollToSubcategory(null);
                    }
                } else if (this.optionScrollAmount < scrollBottom) {
                    // Scroll down
                    double factor = Math.max(0.001D, Math.min(1.0D, 1.0D - Math.pow(0.85D, partialTick)));
                    this.optionScrollAmount += 4 * partialTick + (scrollBottom - this.optionScrollAmount) * factor;
                    if (this.optionScrollAmount > scrollBottom) {
                        this.optionScrollAmount = scrollBottom;
                        this.setScrollToSubcategory(null);
                    }
                } else {
                    this.setScrollToSubcategory(null);
                }
            }

            renderCategoryListScrollBar(guiGraphics);
            renderOptionListScrollBar(guiGraphics);
        }

        if (popupWidget != null) {
            if (popupWidget instanceof LayoutElement popupElement && popupHolder instanceof LayoutElement popupHolderElement) {
                popupElement.setX(popupHolderElement.getX());

                int holderY = popupHolderElement.getY();
                int holderHeight = popupHolderElement.getHeight();
                int popupHeight = popupElement.getHeight();
                if (holderY + holderHeight + popupHeight < this.height) {
                    // Can fit below
                    popupElement.setY(holderY + holderHeight);
                } else if (holderY - popupHeight > 0) {
                    // Can fit above
                    popupElement.setY(holderY - popupHeight);
                } else if (popupHeight < this.height) {
                    // There's enough room, but not enough to position it nicely
                    popupElement.setY(this.height - popupHeight);
                } else {
                    // Not enough room, fit top of popup
                    popupElement.setY(0);
                }
            }
            if (popupWidget instanceof Renderable renderable) {
                LatticeMultiversion.offsetZ(guiGraphics, 1024);
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
                LatticeMultiversion.offsetZ(guiGraphics, -1024);
            }
        }
    }

    private void positionSearchWidgets() {
        this.positionAndRenderSearchWidgets(null, 0, 0, 0);
    }

    private void positionAndRenderSearchWidgets(@Nullable GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var widgets = this.elementSearcher.getSearchedWidgets(this.font, this.buttonWidth);

        if (widgets == null) {
            this.searchContentHeight = 0;
            return;
        }

        int currentY = TOP_PADDING + ITEM_PADDING;

        var focused = this.getFocused();
        for (AbstractWidget widget : widgets) {
            if (this.tickWidgetsOnPosition) {
                this.widgetContext.tick(widget);
            }

            if (!widget.visible) {
                if (focused == widget) {
                    this.clearFocusInternal();
                }
                continue;
            }

            int extraOffset = 0;
            if (widget instanceof StringWidget) {
                extraOffset += 2;
            }
            widget.setPosition(this.width/2-this.buttonWidth/2+extraOffset, currentY - (int) this.searchScrollAmount);
            if (guiGraphics != null) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            currentY += widget.getHeight() + ITEM_PADDING;
        }

        this.searchContentHeight = currentY - TOP_PADDING;
        this.searchScrollAmount = Math.min(this.searchScrollAmount, this.maxScroll(this.searchContentHeight));

        if (this.tickWidgetsOnPosition) {
            this.tickWidgetsOnPosition = false;
            this.widgetContext.finishTick();
        }
    }

    private void positionCategoryWidgets() {
        this.positionAndRenderCategoryWidgets(null, 0, 0, 0);
    }

    private void positionAndRenderCategoryWidgets(@Nullable GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int currentY = TOP_PADDING + ITEM_PADDING - 1;

        for (AbstractButton categoryButton : this.categoryButtons) {
            categoryButton.setPosition(this.width/2-this.buttonWidth-ITEM_PADDING, currentY - (int) this.categoryScrollAmount);
            if (guiGraphics != null) {
                categoryButton.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            currentY += categoryButton.getHeight() + ITEM_PADDING;
        }

        this.categoryContentHeight = currentY - TOP_PADDING - 1;
        this.categoryScrollAmount = Math.min(this.categoryScrollAmount, this.maxScroll(this.categoryContentHeight));
    }

    private void setScrollToSubcategory(LatticeElements subcategory) {
        this.scrollToSubcategory = subcategory;
        this.scrollToSubcategoryStart = -1;
        this.scrollToSubcategoryEnd = -1;
    }

    private void positionOptionWidgets() {
        this.positionAndRenderOptionWidgets(null, 0, 0, 0);
    }

    private void positionAndRenderOptionWidgets(@Nullable GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int currentY = TOP_PADDING + ITEM_PADDING;

        Component title = this.activeCategory == this.rootCategory ? LatticeTextComponents.ROOT_CATEGORY_NAME : this.activeCategory.title;
        if (title != null) {
            if (guiGraphics != null) {
                Component titleWithUnderline = Component.empty().append(title).withStyle(ChatFormatting.UNDERLINE);
                LatticeMultiversion.drawString(guiGraphics, this.font, titleWithUnderline, this.width/2+ITEM_PADDING+2, currentY - (int) this.optionScrollAmount,
                        0xFFFFFFFF);
            }
            currentY += this.font.lineHeight + ITEM_PADDING;
        }

        currentY = this.positionAndRenderOptionWidgetsRecursive(guiGraphics, mouseX, mouseY, partialTick, this.width/2+ITEM_PADDING, this.optionEntries, currentY);

        this.optionContentHeight = currentY - TOP_PADDING;
        this.optionScrollAmount = Math.min(this.optionScrollAmount, this.maxScroll(this.optionContentHeight));

        if (this.tickWidgetsOnPosition) {
            this.tickWidgetsOnPosition = false;
            this.widgetContext.finishTick();
        }
    }

    private int positionAndRenderOptionWidgetsRecursive(@Nullable GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int x, List<AbstractWidget> widgets, int currentY) {
        if (widgets == null) {
            return currentY;
        }
        var focused = this.getFocused();
        for (AbstractWidget widget : widgets) {
            if (this.tickWidgetsOnPosition) {
                this.widgetContext.tick(widget);
            }

            if (!widget.visible) {
                if (focused == widget) {
                    this.clearFocusInternal();
                }
                continue;
            }

            widget.setPosition(x, currentY - (int) this.optionScrollAmount);
            if (guiGraphics != null) {
                widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            boolean scrollTo = widget instanceof SubcategoryButton subcategoryButton && subcategoryButton.getSubcategory() == this.scrollToSubcategory;

            if (scrollTo) {
                this.scrollToSubcategoryStart = currentY - TOP_PADDING;
                this.setFocused(widget);
            }

            currentY += widget.getHeight() + ITEM_PADDING;

            if (widget instanceof WidgetExtraFunctionality extraFunctionality) {
                var extraWidgets = extraFunctionality.extraWidgets();
                if (extraWidgets.isEmpty()) {
                    continue;
                }

                int childX = x + extraFunctionality.extraWidgetHorizonalOffset();

                int startY = currentY - LatticeConfigScreen.ITEM_PADDING;
                currentY = positionAndRenderOptionWidgetsRecursive(guiGraphics, mouseX, mouseY, partialTick, childX, extraWidgets, currentY);
                currentY += LatticeConfigScreen.ITEM_PADDING;

                if (guiGraphics != null) {
                    int startVerticalLineY = startY - (int) this.optionScrollAmount;
                    int endVerticalLineY = currentY - (int) this.optionScrollAmount - LatticeConfigScreen.ITEM_PADDING;
                    if (endVerticalLineY > startVerticalLineY) {
                        int lineX = extraFunctionality.renderVerticalLineForExtraWidgetsAtX();
                        if (lineX >= this.width/2 && lineX < this.width) {
                            guiGraphics.fill(lineX-1, startVerticalLineY, lineX, endVerticalLineY, 0x66FFFFFF);
                            guiGraphics.fill(lineX, startVerticalLineY, lineX+1, endVerticalLineY, 0xBF000000);
                        }
                    }
                }
            }

            if (scrollTo) {
                this.scrollToSubcategoryEnd = currentY - TOP_PADDING;
            }
        }
        return currentY;
    }

    private int getCategoryListScrollBarX() {
        return this.width / 2 - ITEM_PADDING - this.buttonWidth - ITEM_PADDING - SCROLL_BAR_WIDTH;
    }

    private int getOptionListScrollBarX() {
        return this.width / 2 + ITEM_PADDING + this.buttonWidth + ITEM_PADDING;
    }

    private int getSearchListScrollBarX() {
        return this.width/2 + this.buttonWidth/2 + ITEM_PADDING;
    }

    private void renderCategoryListScrollBar(GuiGraphics guiGraphics) {
        this.renderScrollBar(guiGraphics, this.categoryContentHeight, this.categoryScrollAmount, this.getCategoryListScrollBarX());
    }

    private void renderOptionListScrollBar(GuiGraphics guiGraphics) {
        this.renderScrollBar(guiGraphics, this.optionContentHeight, this.optionScrollAmount, this.getOptionListScrollBarX());
    }

    private void renderSearchListScrollBar(GuiGraphics guiGraphics) {
        this.renderScrollBar(guiGraphics, this.searchContentHeight, this.searchScrollAmount, this.getSearchListScrollBarX());
    }

    private void renderScrollBar(GuiGraphics guiGraphics, int contentHeight, double scrollAmount, int x) {
        int maxScroll = this.maxScroll(contentHeight);
        if (maxScroll > 0) {
            double currentScroll = Math.min(scrollAmount, maxScroll);

            int scrollBarHeight = this.scrollableAreaHeight();
            int scrollerHeight = Math.max(32, scrollBarHeight * scrollBarHeight / contentHeight);

            int scrollBarY = TOP_PADDING;
            int scrollerY = scrollBarY + (int) (currentScroll * (scrollBarHeight-scrollerHeight) / maxScroll);

            // Background
            guiGraphics.fill(x, scrollBarY, x+SCROLL_BAR_WIDTH, scrollBarY+scrollBarHeight, 0xFF000000);
            // Scroller background
            guiGraphics.fill(x, scrollerY, x+SCROLL_BAR_WIDTH, scrollerY+scrollerHeight, 0xFF808080);
            // Scroller foregrounds
            guiGraphics.fill(x, scrollerY, x+SCROLL_BAR_WIDTH-1, scrollerY+scrollerHeight-1, 0xFFC0C0C0);
        }
    }


    @Nullable
    private GuiEventListener getPopup() {
        if (this.currentExtraFunctionalityWidget != null) {
            return this.currentExtraFunctionalityWidget.getPopup();
        }
        return null;
    }

    @Nullable
    private WidgetExtraFunctionality getExtraFunctionalityWidget() {
        var focused = this.getFocused();
        if (focused instanceof WidgetExtraFunctionality popup) {
            return popup;
        } else if (focused instanceof WidgetWithText widgetWithText) {
            if (widgetWithText.widget instanceof WidgetExtraFunctionality popup) {
                return popup;
            }
        }
        return null;
    }

    private int scrollableAreaHeight() {
        return this.height-TOP_PADDING-BOTTOM_PADDING;
    }

    private int maxScroll(int contentHeight) {
        int maxScroll = contentHeight - this.scrollableAreaHeight();
        if (maxScroll > ITEM_PADDING) {
            return maxScroll;
        } else {
            return 0;
        }
    }

    @Override
    public void removed() {
        if (this.onClosed != null) {
            this.onClosed.run();
        }
        super.removed();
    }

    public void onClose() {
        if (this.minecraft != null && this.closeTo != null) {
            this.minecraft.setScreen(this.closeTo);
        } else {
            super.onClose();
        }
    }

}
