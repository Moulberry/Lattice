package com.moulberry.lattice.testmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FabricLatticeTestMod implements ModInitializer {

    @Override
    public void onInitialize() {
        LatticeTestMod.onInitialize();
        ClientTickEvents.START_CLIENT_TICK.register(LatticeTestMod::onClientTick);
    }
}
