package com.moulberry.lattice.neoforge;

import com.moulberry.lattice.PlatformImplementation;
import net.neoforged.fml.loading.FMLLoader;

public class NeoforgePlatformImplementation implements PlatformImplementation {
    public String getVersion() {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById("minecraft").versionString();
    }
}