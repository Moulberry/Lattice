package com.moulberry.lattice;

import net.minecraft.SharedConstants;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LatticeMixinConfigPlugin implements IMixinConfigPlugin {

    private String mixinPackage = null;

    private static final Map<String, Integer> protocolVersionForMixin = Map.ofEntries(
        Map.entry("v1201", 0),
        Map.entry("v1202", 764),
        Map.entry("v1204", 765),
        Map.entry("v1206", 766),
        Map.entry("v1216", 771)
    );
    private static final Map<String, Integer> snapshotProtocolVersionForMixin = Map.ofEntries(
        Map.entry("v1201", 0x40000000),
        Map.entry("v1202", 0x40000090),
        Map.entry("v1204", 0x4000009A),
        Map.entry("v1206", 0x400000A9),
        Map.entry("v1216", 0x400000F5)
    );

    private static final Map<String, List<String>> versionedMixinMap = Map.ofEntries(
        Map.entry("multiversion.MixinDrawString", List.of("v1201", "v1216")),
        Map.entry("multiversion.MixinMouseScrolled", List.of("v1201", "v1202")),
        Map.entry("multiversion.MixinNewMultiLineEditBox", List.of("v1201", "v1216")),
        Map.entry("multiversion.MixinOffsetZ", List.of("v1201", "v1216")),
        Map.entry("MixinDropdownWidget", List.of("v1201", "v1202", "v1204", "v1206")),
        Map.entry("MixinLatticeConfigScreen", List.of("v1201", "v1202", "v1216")),
        Map.entry("MixinWidgetWithText", List.of("v1201", "v1202"))
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Objects.requireNonNull(this.mixinPackage);

        if (mixinClassName.startsWith(this.mixinPackage + ".")) {
            String mixinName = mixinClassName.substring(this.mixinPackage.length() + 1);
            List<String> versionsForMixin = versionedMixinMap.get(mixinName);

            if (versionsForMixin == null) {
                throw new RuntimeException("Missing versions for " + mixinName);
            }

            int currentProtocolVersion = SharedConstants.getProtocolVersion();

            Integer latestApplicableVersion = null;
            String applyVersionName = null;
            boolean found = false;

            for (String mixinVersionName : versionsForMixin) {
                int mixinProtocolVersion;
                if (currentProtocolVersion > 0x40000000) {
                    mixinProtocolVersion = snapshotProtocolVersionForMixin.get(mixinVersionName);
                } else {
                    mixinProtocolVersion = protocolVersionForMixin.get(mixinVersionName);
                }

                if (this.mixinPackage.endsWith("." + mixinVersionName)) {
                    found = true;
                }

                if (mixinProtocolVersion <= currentProtocolVersion && (latestApplicableVersion == null || mixinProtocolVersion > latestApplicableVersion)) {
                    latestApplicableVersion = mixinProtocolVersion;
                    applyVersionName = mixinVersionName;
                }
            }

            if (!found) {
                throw new RuntimeException("Mixin " + mixinClassName + " isn't included in versioned map");
            }
            if (applyVersionName == null) {
                throw new RuntimeException("Unable to find applicable version for " + mixinClassName);
            }

            return this.mixinPackage.endsWith("." + applyVersionName);
        } else {
            throw new RuntimeException("Unexpected mixin package: " + mixinClassName);
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

}
