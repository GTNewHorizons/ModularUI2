package com.cleanroommc.modularui;

import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.mariuszgromada.math.mxparser.License;

import java.util.function.Predicate;

@Mod(modid = ModularUI.ID, name = ModularUI.NAME, version = Tags.VERSION, dependencies = ModularUI.DEPENDENCIES, guiFactory = "com.cleanroommc.modularui.config.ModularUIGuiConfigFactory")
public class ModularUI {

    static final String DEPENDENCIES = "required-after:gtnhmixins@[2.0.1,); "
            + "required-after:gtnhlib@[0.2.0,);"
            + "after:NotEnoughItems@[2.3.27-GTNH,);"
            + "after:hodgepodge@[2.0.0,);"
            + "before:gregtech;";

    public static final String ID = "modularui2";
    public static final String NAME = "Modular UI 2";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String BOGO_SORT = "bogosorter";

    @SidedProxy(
            modId = ModularUI.ID,
            clientSide = "com.cleanroommc.modularui.ClientProxy",
            serverSide = "com.cleanroommc.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ModularUI INSTANCE;

    public static final boolean isTestEnv = Launch.blackboard == null;
    public static final boolean isDevEnv = isTestEnv || (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    static {
        // confirm mXparser license
        License.iConfirmNonCommercialUse("GTNewHorizons");
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        proxy.onServerLoad(event);
    }

    public enum Mods {

        BAUBLES(ModIds.BAUBLES),
        BOGOSORTER(ModIds.BOGOSORTER),
        GT5U(ModIds.GT5U, mod -> !Loader.isModLoaded(ModIds.GT6)),
        HODGEPODGE(ModIds.BOGOSORTER),
        NEI(ModIds.NEI),
        NEA(ModIds.NEA);

        public final String id;
        private boolean loaded = false;
        private boolean initialized = false;
        private final Predicate<ModContainer> extraLoadedCheck;

        Mods(String id) {
            this(id, null);
        }

        Mods(String id, @Nullable Predicate<ModContainer> extraLoadedCheck) {
            this.id = id;
            this.extraLoadedCheck = extraLoadedCheck;
        }

        public boolean isLoaded() {
            if (!this.initialized) {
                this.loaded = Loader.isModLoaded(this.id);
                if (this.loaded && this.extraLoadedCheck != null) {
                    this.loaded = this.extraLoadedCheck.test(Loader.instance().getIndexedModList().get(this.id));
                }
                this.initialized = true;
            }
            return this.loaded;
        }
    }

    public static class ModIds {

        public static final String BOGOSORTER = "bogosorter";
        public static final String GT5U = "gregtech";
        public static final String GT6 = "gregapi_post";
        public static final String HODGEPODGE = "hodgepodge";
        public static final String NEI = "NotEnoughItems";
        public static final String NEA = "neverenoughanimations";
        public static final String BAUBLES = "Baubles";
    }
}
