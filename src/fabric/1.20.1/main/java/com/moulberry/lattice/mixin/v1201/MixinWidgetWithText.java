package com.moulberry.lattice.mixin.v1201;

import com.moulberry.lattice.widget.WidgetWithText;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WidgetWithText.class)
@IfMinecraftVersion(maxVersion = "1.20.1")
public abstract class MixinWidgetWithText extends AbstractWidget {

    @Shadow @Final public AbstractWidget widget;

    public MixinWidgetWithText(int $$0, int $$1, int $$2, int $$3, Component $$4) {
        super($$0, $$1, $$2, $$3, $$4);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        int right = this.widget.getX() + this.widget.getWidth();
        int bottom = this.widget.getY() + this.widget.getHeight();
        if (mouseX >= this.widget.getX() && mouseY >= this.widget.getY() && mouseX <= right && mouseY <= bottom) {
            return this.widget.mouseScrolled(mouseX, mouseY, scrollY);
        }
        return false;
    }

}
