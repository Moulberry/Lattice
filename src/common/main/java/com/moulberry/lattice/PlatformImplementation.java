package com.moulberry.lattice;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface PlatformImplementation {

    String getVersion();

    @ApiStatus.Internal
    static PlatformImplementation getImplementation() {
        String impl;
        if (isLoaded("net.neoforged.fml.loading.FMLLoader")) {
            impl = "com.moulberry.lattice.neoforge.NeoforgePlatformImplementation";
        } else if (isLoaded("net.fabricmc.loader.api.FabricLoader")) {
            impl = "com.moulberry.lattice.fabric.FabricPlatformImplementation";
        } else {
            throw new IllegalStateException("No implementation found for PlatformImplementation");
        }

        try {
            return (PlatformImplementation) Class.forName(impl).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load Platform Implementation", e);
        }
    }


    private static boolean isLoaded(@NotNull String className) {
        try {
            Class.forName(className, false, PlatformImplementation.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
