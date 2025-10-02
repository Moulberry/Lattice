package com.moulberry.lattice;

import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.element.LatticeElements;
import com.moulberry.lattice.widget.CategoryStringWidget;
import com.moulberry.lattice.widget.CenteredStringWidget;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
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
public class LatticeElementSearcher {

    private final LatticeElements root;

    private static final class SearchedElement {
        private final List<LatticeElements> categoryPath;
        private boolean categoryMatchesSearch;
        private final LatticeElement element;

        public SearchedElement(List<LatticeElements> categoryPath, boolean categoryMatchesSearch, LatticeElement element) {
            this.categoryPath = categoryPath;
            this.categoryMatchesSearch = categoryMatchesSearch;
            this.element = element;
        }
    }

    private String lastSearch = null;
    private final List<SearchedElement> searchedElements = new ArrayList<>();
    private List<AbstractWidget> searchedWidgets = null;
    private final LatticeWidgetContext widgetContext;

    private Font lastWidgetFont = null;
    private int lastWidgetWidth = 0;

    public LatticeElementSearcher(LatticeElements root, LatticeWidgetContext widgetContext) {
        this.root = root;
        this.widgetContext = widgetContext;
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
            performSearch(List.of(this.root), false, search);
        } else {
            this.lastSearch = search;
            refineSearch(search);
        }
    }

    private void performSearch(List<LatticeElements> currentPath, boolean categoryMatchesSearch, String search) {
        if (currentPath == null || currentPath.isEmpty()) {
            throw new IllegalArgumentException();
        }

        LatticeElements elements = currentPath.get(currentPath.size() - 1);
        if (elements.isEmpty()) {
            return;
        }

        if (!categoryMatchesSearch) {
            categoryMatchesSearch = this.matchesSearch(search, elements);
        }

        for (LatticeElement option : elements.options) {
            boolean matches = categoryMatchesSearch || matchesSearch(search, option);

            if (matches) {
                SearchedElement searchedElement = new SearchedElement(currentPath, categoryMatchesSearch, option);
                this.searchedElements.add(searchedElement);
            }
        }

        List<LatticeElements> currentPathWithoutRoot;
        if (elements == this.root) {
            currentPathWithoutRoot = new ArrayList<>();
            categoryMatchesSearch = false;
        } else {
            currentPathWithoutRoot = currentPath;
        }

        for (LatticeElements subcategory : elements.subcategories) {
            List<LatticeElements> subPath = new ArrayList<>(currentPathWithoutRoot);
            subPath.add(subcategory);
            performSearch(Collections.unmodifiableList(subPath), categoryMatchesSearch, search);
        }
    }

    private void refineSearch(String search) {
        var iterator = this.searchedElements.iterator();

        boolean lastCategoryMatchesSearch = false;
        List<LatticeElements> lastCategoryPath = null;

        Object2BooleanOpenHashMap<LatticeElements> matchesSearch = new Object2BooleanOpenHashMap<>();

        while (iterator.hasNext()) {
            SearchedElement searchedElement = iterator.next();

            List<LatticeElements> categoryPath = searchedElement.categoryPath;

            if (categoryPath != lastCategoryPath) {
                lastCategoryPath = categoryPath;

                lastCategoryMatchesSearch = false;
                for (LatticeElements category : categoryPath) {
                    lastCategoryMatchesSearch = matchesSearch.computeIfAbsent(category, elements -> this.matchesSearch(search, (LatticeElements) elements));
                    if (lastCategoryMatchesSearch) {
                        break;
                    }
                }
            }

            boolean matches = lastCategoryMatchesSearch || matchesSearch(search, searchedElement.element);

            if (!matches) {
                iterator.remove();
            } else {
                searchedElement.categoryMatchesSearch = lastCategoryMatchesSearch;
            }
        }
    }

    private boolean matchesSearch(String search, LatticeElements category) {
        Component title;
        if (category == this.root) {
            title = LatticeTextComponents.ROOT_CATEGORY_NAME;
        } else {
            title = category.getTitleOrDefault();
        }

        String titleString = title.getString();
        return titleString.toLowerCase(Locale.ROOT).contains(search);
    }

    private static boolean matchesSearch(String search, LatticeElement option) {
        if (!option.canBeSearched()) {
            return false;
        }

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

        List<LatticeElements> lastCategoryPath = null;
        for (SearchedElement searchedElement : this.searchedElements) {
            if (searchedElement.categoryPath != lastCategoryPath) {
                lastCategoryPath = searchedElement.categoryPath;

                List<Component> components = new ArrayList<>(lastCategoryPath.size());
                for (LatticeElements category : lastCategoryPath) {
                    Component title;
                    if (category == this.root) {
                        title = LatticeTextComponents.ROOT_CATEGORY_NAME;
                    } else {
                        title = category.getTitleOrDefault();
                    }

                    if (searchedElement.categoryMatchesSearch) {
                        Component styledTitle = applySearchStyleToComponent(title, searchCharArray);
                        if (styledTitle != null) {
                            title = styledTitle;
                        }
                    }

                    title = Component.empty().append(title).withStyle(ChatFormatting.UNDERLINE);

                    components.add(title);
                }

                this.searchedWidgets.add(new CategoryStringWidget(0, 0, width, font.lineHeight, lastCategoryPath, components, font));
            }

            Component title = searchedElement.element.title();
            Component description = searchedElement.element.description();

            if (!searchedElement.categoryMatchesSearch) {
                Component maybeBoldTitle = applySearchStyleToComponent(title, searchCharArray);
                if (maybeBoldTitle != null) {
                    title = maybeBoldTitle;
                } else if (description != null) {
                    Component maybeBoldDescription = applySearchStyleToComponent(description, searchCharArray);
                    if (maybeBoldDescription != null) {
                        description = maybeBoldDescription;
                    }
                }
            }

            var widget = this.widgetContext.create(searchedElement.element, title, description, width);
            if (widget != null) {
                this.searchedWidgets.add(widget);
            }
        }

        if (this.searchedWidgets.isEmpty()) {
            Component message = Component.translatable("lattice.no_results_found", Component.literal(this.lastSearch));
            this.searchedWidgets.add(new CenteredStringWidget(width, font.lineHeight*3, message, font));
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
                        int startStyledIndex = Math.max(0, index - search.length + 1);

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
                if (state.searchPosition == 0) {
                    String remaining = string.substring(usedIndex);
                    newComponent.append(Component.literal(remaining).setStyle(style));
                } else {
                    int startStyledIndex = Math.max(0, stringLength - state.searchPosition);

                    if (usedIndex < startStyledIndex) {
                        newComponent.append(Component.literal(string.substring(usedIndex, startStyledIndex)).setStyle(style));
                    }

                    String remainingStyled = string.substring(startStyledIndex);
                    MaybeMatchingFragment fragment = new MaybeMatchingFragment();
                    fragment.style = style;
                    fragment.content = remainingStyled;
                    state.maybeMatchingFragments.add(fragment);
                }
            }

            return Optional.empty();
        }, Style.EMPTY);

        for (MaybeMatchingFragment fragment : state.maybeMatchingFragments) {
            newComponent.append(Component.literal(fragment.content).setStyle(fragment.style));
        }

        if (state.appliedSearchStyle) {
            return newComponent;
        } else {
            return null;
        }
    }

}
