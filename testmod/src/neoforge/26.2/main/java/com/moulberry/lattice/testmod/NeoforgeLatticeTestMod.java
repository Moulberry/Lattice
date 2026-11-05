package com.moulberry.lattice.testmod;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "lattice_testmod", dist = Dist.CLIENT)
public class NeoforgeLatticeTestMod {

    public NeoforgeLatticeTestMod(IEventBus modBus) {
        modBus.addListener(this::onClientSetup);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }

    public void onClientTick(ClientTickEvent.Pre event) {
        LatticeTestMod.onClientTick(Minecraft.getInstance());
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        LatticeTestMod.onInitialize();
    }
}
