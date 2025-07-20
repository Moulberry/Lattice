package com.moulberry.lattice.multiversion;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LatticeMultiversion {

    public static void drawString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color) {
        // Implemented by MixinDrawString
        throw new UnsupportedOperationException();
    }

    public static void drawString(GuiGraphics guiGraphics, Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color) {
        // Implemented by MixinDrawString
        throw new UnsupportedOperationException();
    }

    public static boolean callMouseScrolled(GuiEventListener eventListener, double mouseX, double mouseY, double scrollY) {
        // Implemented by MixinMouseScrolled
        throw new UnsupportedOperationException();
    }

    public static MultiLineEditBox newMultiLineEditBox(Font font, int width, int height, Component title) {
        // Implemented by MixinNewMultiLineEditBox
        throw new UnsupportedOperationException();
    }

    public static void offsetZ(GuiGraphics guiGraphics, double z) {
        // Implemented by MixinOffsetZ on supported versions (<1.21.6)
    }

}
