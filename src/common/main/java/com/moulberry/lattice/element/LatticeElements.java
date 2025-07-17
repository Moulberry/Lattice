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

    LatticeElements(@Nullable Component title) {
        this.title = title;
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

    public static LatticeElements empty(Component title) {
        return new LatticeElements(title);
    }

    public static LatticeElements fromAnnotations(Component title, Object config) throws LatticeFieldToOptionException {
        Options minecraftOptions = Minecraft.getInstance().options;
        if (minecraftOptions == null) {
            throw new RuntimeException("Minecraft options haven't been initialized yet!");
        }

        LatticeElements elements = new LatticeElements(title);
        List<KeyMapping> keyMappings = new ArrayList<>(List.of(minecraftOptions.keyMappings));
        ElementReflection.addElementsFromClass(config, elements, keyMappings);
        return elements;
    }

}
