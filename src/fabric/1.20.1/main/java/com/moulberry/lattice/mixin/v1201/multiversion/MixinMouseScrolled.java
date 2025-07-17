package com.moulberry.lattice.mixin.v1201.multiversion;

import com.moulberry.lattice.LatticeConfigScreen;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

@IfMinecraftVersion(maxVersion = "1.20.1")
@Mixin(LatticeMultiversion.class)
public class MixinMouseScrolled {

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public static boolean callMouseScrolled(GuiEventListener eventListener, double mouseX, double mouseY, double scrollY) {
        return eventListener.mouseScrolled(mouseX, mouseY, scrollY);
    }

}
