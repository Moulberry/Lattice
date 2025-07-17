package com.moulberry.lattice.mixin.v1201;

import com.moulberry.lattice.LatticeConfigScreen;
import com.moulberry.mixinconstraints.annotations.IfMinecraftVersion;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfMinecraftVersion(maxVersion = "1.20.1")
@Mixin(LatticeConfigScreen.class)
public abstract class MixinLatticeConfigScreen extends Screen {

    protected MixinLatticeConfigScreen(Component title) {
        super(title);
    }

    @Shadow
    protected abstract void renderDividersAndBackground(GuiGraphics guiGraphics);

    @Shadow
    protected abstract boolean mouseScrolledInternal(double mouseX, double mouseY, double scrollY);

    @Override
    public void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
        this.renderDividersAndBackground(guiGraphics);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        this.renderBackground(guiGraphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (this.mouseScrolledInternal(mouseX, mouseY, scrollY)) {
            return true;
        } else {
            return super.mouseScrolled(mouseX, mouseY, scrollY);
        }
    }
}
