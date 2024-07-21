package com.cleanroommc.modularui.core;

import com.cleanroommc.modularui.mixinplugin.Mixins;
import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class ModularUICore implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public static final Logger LOGGER = LogManager.getLogger("modularui2");
    private static Boolean isDevEnv;

    public static boolean isDevEnv() {
        return isDevEnv;
    }

    @Override
    public String getMixinConfig() {
        return "mixins.modularui2.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        final List<String> mixins = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.EARLY && mixin.shouldLoad(loadedCoreMods, Collections.emptySet())) {
                mixins.add(mixin.mixinClass);
            }
        }
        return mixins;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"com.cleanroommc.modularui.core.ModularUITransformer"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isDevEnv = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
