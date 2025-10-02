package com.moulberry.lattice.mixin.v1201.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LatticeMultiversion.class)
public class MixinGetWindowHandle {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static long getWindowHandle() {
        return Minecraft.getInstance().getWindow().getWindow();
    }

}
