package com.moulberry.lattice.element;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LatticeElements {

    public List<LatticeElement> options = new ArrayList<>();
    public List<LatticeElements> subcategories = new ArrayList<>();
    public @Nullable Component title;

    LatticeElements(@Nullable Component title) {
        this.title = title;
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
