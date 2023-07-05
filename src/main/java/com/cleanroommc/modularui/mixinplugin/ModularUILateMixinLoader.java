package com.cleanroommc.modularui.mixinplugin;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ModularUILateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.modularui.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        final List<String> mixins = new ArrayList<>();
        for (Mixins mixin : Mixins.values()) {
            if (mixin.phase == Mixins.Phase.LATE && mixin.shouldLoad(Collections.emptySet(), loadedMods)) {
                mixins.add(mixin.mixinClass);
            }
        }
        return mixins;
    }
}
