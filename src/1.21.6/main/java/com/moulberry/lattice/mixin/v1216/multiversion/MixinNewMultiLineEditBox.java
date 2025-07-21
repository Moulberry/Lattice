package com.moulberry.lattice.mixin.v1216.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
public class MixinNewMultiLineEditBox {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static MultiLineEditBox newMultiLineEditBox(Font font, int width, int height, Component title) {
        return MultiLineEditBox.builder().build(font, width, height, title);
    }

}
