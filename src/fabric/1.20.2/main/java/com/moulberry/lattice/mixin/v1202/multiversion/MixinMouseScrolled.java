package com.moulberry.lattice.mixin.v1202.multiversion;

import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@IfMinecraftVersion(minVersion = "1.20.2")
@Mixin(LatticeMultiversion.class)
public class MixinMouseScrolled {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static boolean callMouseScrolled(GuiEventListener eventListener, double mouseX, double mouseY, double scrollY) {
        return eventListener.mouseScrolled(mouseX, mouseY, 0, scrollY);
    }

}
