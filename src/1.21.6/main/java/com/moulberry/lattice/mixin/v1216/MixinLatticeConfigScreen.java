package com.moulberry.lattice.mixin.v1216;

import com.moulberry.lattice.LatticeConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LatticeConfigScreen.class)
public abstract class MixinLatticeConfigScreen extends Screen {

    protected MixinLatticeConfigScreen(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void renderDividersAndBackground(GuiGraphics guiGraphics);

    @Shadow
    protected abstract boolean mouseScrolledInternal(double mouseX, double mouseY, double scrollY);

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        this.renderDividersAndBackground(guiGraphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.mouseScrolledInternal(mouseX, mouseY, scrollY)) {
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
    }

}
