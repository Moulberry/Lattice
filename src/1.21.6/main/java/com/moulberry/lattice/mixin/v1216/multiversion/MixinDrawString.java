package com.moulberry.lattice.mixin.v1216.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
@IfMinecraftVersion(minVersion = "1.21.6")
public class MixinDrawString {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static void drawString(GuiGraphics guiGraphics, Font font, Component component, int x, int y, int color) {
        guiGraphics.drawString(font, component, x, y, color);
    }

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static void drawString(GuiGraphics guiGraphics, Font font, FormattedCharSequence formattedCharSequence, int x, int y, int color) {
        guiGraphics.drawString(font, formattedCharSequence, x, y, color);
    }

}
