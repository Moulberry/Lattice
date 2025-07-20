package com.moulberry.lattice;

import com.moulberry.lattice.element.LatticeElement;
import com.moulberry.lattice.element.LatticeElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Lattice {

    public static Screen createConfigScreen(LatticeElements elements, @Nullable Runnable onClosed, @Nullable Screen closeTo) {
        return new LatticeConfigScreen(elements, onClosed, closeTo);
    }

    public static void performTest(LatticeElements elements) {
        performWidgetTest(elements);

        Screen screen = createConfigScreen(elements, null, null);
        screen.init(Minecraft.getInstance(), 480, 270);
    }

    private static void performWidgetTest(LatticeElements elements) {
        Font font = Minecraft.getInstance().font;
        for (LatticeElement option : elements.options) {
            option.createInnerWidget(font, Component.literal("Title"), null, 100);
        }
        for (LatticeElements subcategory : elements.subcategories) {
            performWidgetTest(subcategory);
        }
    }

}
