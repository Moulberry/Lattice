package com.moulberry.lattice.testmod;

import com.moulberry.lattice.Lattice;
import com.moulberry.lattice.element.LatticeElements;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LatticeTestMod implements ModInitializer {

    @Override
    public void onInitialize() {
        TestConfig testConfig = new TestConfig();

        Minecraft.getInstance().submit(() -> {
            LatticeElements elements = LatticeElements.fromAnnotations(Component.literal("Test Config"), testConfig);
            Lattice.performTest(elements);
        });

        ClientTickEvents.START_CLIENT_TICK.register(minecraft -> {
            long window = minecraft.getWindow().getWindow();
            Screen screen = minecraft.screen;
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_K) != 0 && (screen == null || screen instanceof TitleScreen)) {
                LatticeElements elements = LatticeElements.fromAnnotations(Component.literal("Test Config"), testConfig);
                minecraft.setScreen(Lattice.createConfigScreen(elements, null, screen));
            }
        });
    }

}
