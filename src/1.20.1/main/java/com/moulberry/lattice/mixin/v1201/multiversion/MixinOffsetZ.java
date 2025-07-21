package com.moulberry.lattice.mixin.v1201.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
public class MixinOffsetZ {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static void offsetZ(GuiGraphics guiGraphics, double z) {
        guiGraphics.pose().translate(0, 0, z);
    }

}
