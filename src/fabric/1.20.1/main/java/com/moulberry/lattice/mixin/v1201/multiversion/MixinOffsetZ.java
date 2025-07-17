package com.moulberry.lattice.mixin.v1201.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
@IfMinecraftVersion(maxVersion = "1.21.5")
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
