package com.moulberry.lattice.mixin.v1201.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
@IfMinecraftVersion(maxVersion = "1.21.5")
public class MixinNewMultiLineEditBox {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static MultiLineEditBox newMultiLineEditBox(Font font, int width, int height, Component title) {
        return new MultiLineEditBox(font, 0, 0, width, height, CommonComponents.EMPTY, title);
    }

}
