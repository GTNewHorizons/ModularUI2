package com.cleanroommc.modularui.holoui;

import com.cleanroommc.modularui.core.mixins.early.minecraft.EntityAccessor;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularScreen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Highly experimental
 */
@ApiStatus.Experimental
public class HoloScreenEntity extends Entity {

    private GuiContainerWrapper wrapper;
    private ModularScreen screen;
    private final Plane3D plane3D;
    private static final int ORIENTATION = 16;

    public HoloScreenEntity(World worldIn, Plane3D plane3D) {
        super(worldIn);
        this.plane3D = plane3D;
    }

    public HoloScreenEntity(World world) {
        this(world, new Plane3D());
    }

    public void setScreen(ModularScreen screen) {
        this.screen = screen;
        this.wrapper = new GuiContainerWrapper(new ModularContainer(), screen);
        this.wrapper.setWorldAndResolution(Minecraft.getMinecraft(), (int) this.plane3D.getWidth(), (int) this.plane3D.getHeight());
    }

    public ModularScreen getScreen() {
        return this.screen;
    }

    public GuiContainerWrapper getWrapper() {
        return this.wrapper;
    }

    public void spawnInWorld() {
        worldObj.spawnEntityInWorld(this);
    }

    public void setOrientation(ScreenOrientation orientation) {
        this.dataWatcher.updateObject(ORIENTATION, (byte) orientation.ordinal());
    }

    public ScreenOrientation getOrientation() {
        return ScreenOrientation.values()[this.dataWatcher.getWatchableObjectByte(ORIENTATION)];
    }

    public Plane3D getPlane3D() {
        return this.plane3D;
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(ORIENTATION, (byte) 1);
    }

    @Override
    public void onEntityUpdate() {
        this.worldObj.theProfiler.startSection("entityBaseTick");
        this.prevDistanceWalkedModified = this.distanceWalkedModified;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        if (this.worldObj.isRemote) {
            this.extinguish();
        }
        if (this.posY < -64.0D) {
            this.kill();
        }

        if (this.worldObj.isRemote) {
            int w = (int) this.plane3D.getWidth(), h = (int) this.plane3D.getHeight();
            if (w != this.wrapper.width || h != this.wrapper.height) {
                this.wrapper.setWorldAndResolution(Minecraft.getMinecraft(), w, h);
            }
        }

        ((EntityAccessor) this).setFirstUpdate(false);
        this.worldObj.theProfiler.endSection();
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    protected void readEntityFromNBT(@NotNull NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(@NotNull NBTTagCompound compound) {

    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean isCreatureType(@NotNull EnumCreatureType type, boolean forSpawnCount) {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender(float p_70070_1_) {
        return 15728880;
    }
}
