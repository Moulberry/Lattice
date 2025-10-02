package com.moulberry.lattice.mixin.v1219;

import com.moulberry.lattice.widget.DropdownWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(DropdownWidget.Entry.class)
public abstract class MixinDropdownWidgetEntry extends ObjectSelectionList.Entry {

    @Shadow
    protected abstract void actuallyRender(GuiGraphics guiGraphics, int x, int y);

    /**
     * @author Moulberry
     * @reason Implementation
     */
    @Overwrite
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
        this.actuallyRender(guiGraphics, this.getContentX(), this.getContentY());
    }
}
