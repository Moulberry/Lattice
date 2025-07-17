package com.moulberry.lattice;

import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.element.LatticeElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ApiStatus.Internal
public class ElementSearcher {

    private final LatticeElements root;

    record SearchedElement(Component categoryTree, LatticeElement element) {}

    private String lastSearch = null;
    private final List<SearchedElement> searchedElements = new ArrayList<>();
    private List<AbstractWidget> searchedWidgets = null;

    private Font lastWidgetFont = null;
    private int lastWidgetWidth = 0;

    public ElementSearcher(LatticeElements root) {
        this.root = root;
    }

    public void search(String search) {
        search = search.toLowerCase(Locale.ROOT).strip();

        if (Objects.equals(search, this.lastSearch)) {
            return;
        }

        this.searchedWidgets = null;

        if (search.isEmpty()) {
            this.lastSearch = null;
            return;
        }

        if (this.lastSearch == null || this.lastSearch.isEmpty() || !search.startsWith(this.lastSearch)) {
            this.lastSearch = search;
            this.searchedElements.clear();
            performSearch(this.root, null, search);
        } else {
            this.lastSearch = search;
            refineSearch(search);
        }
    }

    private void performSearch(LatticeElements elements, @Nullable Component titlePath, String search) {
        Component title;
        Component titleForChildren;

        if (elements == this.root || elements.title == null) {
            title = LatticeTextComponents.DEFAULT_CATEGORY_NAME.copy().withStyle(ChatFormatting.UNDERLINE);
            if (titlePath != null) {
                title = Component.empty().append(titlePath).append(" / ").append(title);
            }
            titleForChildren = null;
        } else {
            title = Component.empty().append(elements.title).withStyle(ChatFormatting.UNDERLINE);
            if (titlePath != null) {
                title = Component.empty().append(titlePath).append(" / ").append(title);
            }
            titleForChildren = title;
        }

        for (LatticeElement option : elements.options) {
            boolean matches = matchesSearch(search, option);

            if (matches) {
                SearchedElement searchedElement = new SearchedElement(title, option);
                this.searchedElements.add(searchedElement);
            }
        }

        for (LatticeElements subcategory : elements.subcategories) {
            performSearch(subcategory, titleForChildren, search);
        }
    }

    private void refineSearch(String search) {
        var iterator = this.searchedElements.iterator();
        while (iterator.hasNext()) {
            SearchedElement searchedElement = iterator.next();
            boolean matches = matchesSearch(search, searchedElement.element);

            if (!matches) {
                iterator.remove();
            }
        }
    }

    private static boolean matchesSearch(String search, LatticeElement option) {
        String primary = option.searchKeyPrimary();
        if (primary != null && primary.contains(search)) {
            return true;
        }

        String secondary = option.searchKeySecondary();
        return secondary != null && secondary.contains(search);
    }

    @Nullable
    public List<AbstractWidget> getSearchedWidgets(Font font, int width) {
        if (this.lastSearch == null) {
            return null;
        }
        if (this.searchedWidgets != null && font == this.lastWidgetFont && width == this.lastWidgetWidth) {
            return this.searchedWidgets;
        }

        this.searchedWidgets = new ArrayList<>();
        this.lastWidgetFont = font;
        this.lastWidgetWidth = width;

        char[] searchCharArray = this.lastSearch.toCharArray();

        Component lastCategoryTree = null;
        for (SearchedElement searchedElement : this.searchedElements) {
            Component categoryTree = searchedElement.categoryTree;

            if (categoryTree != lastCategoryTree) {
                lastCategoryTree = categoryTree;
                this.searchedWidgets.add(new StringWidget(width, font.lineHeight, categoryTree, font).alignLeft());
            }

            Component title = searchedElement.element.title();
            Component description = searchedElement.element.description();

            Component maybeBoldTitle = applySearchStyleToComponent(title, searchCharArray);
            if (maybeBoldTitle != null) {
                title = maybeBoldTitle;
            } else if (description != null) {
                Component maybeBoldDescription = applySearchStyleToComponent(description, searchCharArray);
                if (maybeBoldDescription != null) {
                    description = maybeBoldDescription;
                }
            }

            this.searchedWidgets.add(searchedElement.element.createWidget(font, title, description, width));
        }

        if (this.searchedWidgets.isEmpty()) {
            String message = "No results found for '" + this.lastSearch + "'";
            this.searchedWidgets.add(new StringWidget(width, font.lineHeight*3, Component.literal(message), font).alignCenter());
        }

        return this.searchedWidgets;
    }

    private static final Style SEARCH_STYLE = Style.EMPTY.withUnderlined(true).withColor(0xFFFFFF00);

    private static @Nullable Component applySearchStyleToComponent(Component component, char[] search) {
        if (search.length == 0) {
            return null;
        }

        MutableComponent newComponent = Component.empty();

        class MaybeMatchingFragment {
            Style style;
            String content;
        }

        class ApplySearchStyleToComponentState {
            List<MaybeMatchingFragment> maybeMatchingFragments = new ArrayList<>();
            int searchPosition = 0;
            boolean appliedSearchStyle = false;
        }

        ApplySearchStyleToComponentState state = new ApplySearchStyleToComponentState();

        component.visit((style, string) -> {
            if (string.isEmpty()) {
                return Optional.empty();
            }

            int usedIndex = 0;

            int stringLength = string.length();
            for (int index = 0; index < stringLength; index++) {
                char c = string.charAt(index);

                if (Character.toLowerCase(c) == search[state.searchPosition]) {
                    state.searchPosition += 1;
                    if (state.searchPosition == search.length) {
                        int startStyledIndex = index - search.length + 1;

                        if (usedIndex < startStyledIndex) {
                            newComponent.append(Component.literal(string.substring(usedIndex, startStyledIndex)).setStyle(style));
                        }

                        for (MaybeMatchingFragment fragment : state.maybeMatchingFragments) {
                            newComponent.append(Component.literal(fragment.content).setStyle(fragment.style).withStyle(SEARCH_STYLE));
                        }
                        newComponent.append(Component.literal(string.substring(startStyledIndex, index+1)).setStyle(style).withStyle(SEARCH_STYLE));

                        state.searchPosition = 0;
                        state.maybeMatchingFragments.clear();
                        state.appliedSearchStyle = true;
                        usedIndex = index+1;
                    }
                } else {
                    state.searchPosition = 0;
                    if (!state.maybeMatchingFragments.isEmpty()) {
                        for (MaybeMatchingFragment fragment : state.maybeMatchingFragments) {
                            newComponent.append(Component.literal(fragment.content).setStyle(fragment.style));
                        }
                        state.maybeMatchingFragments.clear();
                    }
                }
            }

            if (usedIndex < stringLength) {
                String remaining = string.substring(usedIndex);
                if (state.searchPosition == 0) {
                    newComponent.append(Component.literal(remaining).setStyle(style));
                } else {
                    MaybeMatchingFragment fragment = new MaybeMatchingFragment();
                    fragment.style = style;
                    fragment.content = remaining;
                    state.maybeMatchingFragments.add(fragment);
                }
            }

            return Optional.empty();
        }, Style.EMPTY);

        if (state.appliedSearchStyle) {
            return newComponent;
        } else {
            return null;
        }
    }

}
