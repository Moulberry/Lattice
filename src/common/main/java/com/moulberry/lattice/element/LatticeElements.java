package com.moulberry.lattice.element;

import com.moulberry.lattice.LatticeTextComponents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LatticeElements {

    public List<LatticeElement> options = new ArrayList<>();
    public List<LatticeElements> subcategories = new ArrayList<>();
    public @Nullable Component title;

    private LatticeDynamicCondition disabledDynamic = null;
    private LatticeDynamicCondition hiddenDynamic = null;

    LatticeElements(@Nullable Component title) {
        this.title = title;
    }

    public static LatticeElements empty(Component title) {
        return new LatticeElements(title);
    }

    public static LatticeElements fromAnnotations(Component title, Object config) throws LatticeFieldToOptionException {
        ElementReflection elementReflection = new ElementReflection();

        LatticeElements elements = new LatticeElements(title);
        elementReflection.addElementsFromClass(config, elements);
        return elements;
    }

    public @NotNull Component getTitleOrDefault() {
        return Objects.requireNonNullElse(this.title, LatticeTextComponents.UNNAMED_CATEGORY);
    }

    public boolean isEmpty() {
        if (!this.options.isEmpty()) {
            return false;
        }

        for (LatticeElements subcategory : this.subcategories) {
            if (!subcategory.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public void disabledDynamic(LatticeDynamicCondition disabledDynamic) {
        this.disabledDynamic = disabledDynamic;
    }

    public void hiddenDynamic(LatticeDynamicCondition hiddenDynamic) {
        this.hiddenDynamic = hiddenDynamic;
    }

    public LatticeDynamicCondition disabledDynamic() {
        return this.disabledDynamic;
    }

    public LatticeDynamicCondition hiddenDynamic() {
        return this.hiddenDynamic;
    }

}
