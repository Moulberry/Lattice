package com.moulberry.lattice.testmod;

import com.moulberry.lattice.Lattice;
import com.moulberry.lattice.element.LatticeElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod("lattice_testmod")
public class LatticeTestMod {

    private final TestConfig testConfig = new TestConfig();

    public LatticeTestMod() {
        NeoForge.EVENT_BUS.addListener(this::onStartTick);
    }

    public void onStartTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        long window = minecraft.getWindow().getWindow();
        Screen screen = minecraft.screen;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_K) != 0 && (screen == null || screen instanceof TitleScreen)) {
            LatticeElements elements = LatticeElements.fromAnnotations(Component.literal("Test Config"), testConfig);
            minecraft.setScreen(Lattice.createConfigScreen(elements, null, screen));
        }
    }

}
