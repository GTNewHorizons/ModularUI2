package com.cleanroommc.modularui;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ModularUI.ID, name = "Modular UI 2", version = Tags.VERSION, dependencies = ModularUI.DEPENDENCIES, guiFactory = "com.cleanroommc.modularui.config.GuiFactory")
public class ModularUI {

    static final String DEPENDENCIES = "required-after:gtnhmixins@[2.0.1,); "
        + "required-after:gtnhlib@[0.2.0,);"
        + "after:NotEnoughItems@[2.3.27-GTNH,);"
        + "after:hodgepodge@[2.0.0,);"
        + "before:gregtech";

    public static final String ID = "modularui2";

    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final String MODID_NEI = "NotEnoughItems";
    public static final String MODID_GT5U = "gregtech";
    public static final String MODID_GT6 = "gregapi_post";
    public static final boolean isNEILoaded = Loader.isModLoaded(MODID_NEI);
    public static final boolean isGT5ULoaded = Loader.isModLoaded(MODID_GT5U) && !Loader.isModLoaded(MODID_GT6);
    public static final boolean isHodgepodgeLoaded = Loader.isModLoaded("hodgepodge");

    @SidedProxy(
        modId = ModularUI.ID,
        clientSide = "com.cleanroommc.modularui.ClientProxy",
        serverSide = "com.cleanroommc.modularui.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static ModularUI INSTANCE;

    public static final boolean isDevEnv = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void onLoadComplete(FMLLoadCompleteEvent event) {
    }

    @Mod.EventHandler
    public void onServerLoad(FMLServerStartingEvent event) {
        proxy.onServerLoad(event);
    }
}
