package com.moulberry.lattice.testmod;

import com.moulberry.lattice.Lattice;
import com.moulberry.lattice.element.LatticeElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class LatticeTestMod {

    public static TestConfig testConfig = new TestConfig();

    public static void onInitialize() {

        Minecraft.getInstance().submit(() -> {
            LatticeElements elements = LatticeElements.fromAnnotations(Component.literal("Test Config"), testConfig);
            Lattice.performTest(elements);
        });
    }

    public static void onClientTick(Minecraft minecraft) {
        long window = Minecraft.getInstance().getWindow().handle();
        Screen screen = minecraft.gui.screen();
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_K) != 0 && (screen == null || screen instanceof TitleScreen)) {
            LatticeElements elements = LatticeElements.fromAnnotations(Component.literal("Test Config"), testConfig);
            minecraft.gui.setScreen(Lattice.createConfigScreen(elements, null, screen));
        }
    }
}
