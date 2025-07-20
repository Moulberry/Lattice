package com.moulberry.lattice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LatticeMixinConfigPlugin implements IMixinConfigPlugin {

    private String mixinPackage = null;
    private static int dataVersion = -1;

    private static final Map<String, Integer> dataVersionForMixin = Map.ofEntries(
        Map.entry("v1201", 0),
        Map.entry("v1202", 3572),
        Map.entry("v1204", 3693),
        Map.entry("v1206", 3829),
        Map.entry("v1216", 4430)
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
            if (dataVersion == -1) {
                try {
                    try (InputStream inputStream = LatticeMixinConfigPlugin.class.getResourceAsStream("/version.json")) {
                        if (inputStream == null) {
                            throw new RuntimeException("/version.json is not present!");
                        }
                        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                            dataVersion = jsonObject.get("world_version").getAsInt();
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Unable to parse Minecraft version information", e);
                }
            }

            String mixinName = mixinClassName.substring(this.mixinPackage.length() + 1);
            List<String> versionsForMixin = versionedMixinMap.get(mixinName);

            if (versionsForMixin == null) {
                throw new RuntimeException("Missing versions for " + mixinName);
            }

            Integer latestApplicableVersion = null;
            String applyVersionName = null;
            boolean found = false;

            for (String mixinVersionName : versionsForMixin) {
                int mixinDataVersion = dataVersionForMixin.get(mixinVersionName);

                if (this.mixinPackage.endsWith("." + mixinVersionName)) {
                    found = true;
                }

                if (mixinDataVersion <= this.dataVersion && (latestApplicableVersion == null || mixinDataVersion > latestApplicableVersion)) {
                    latestApplicableVersion = mixinDataVersion;
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
