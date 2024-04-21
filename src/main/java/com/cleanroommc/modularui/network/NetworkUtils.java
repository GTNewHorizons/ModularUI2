package com.cleanroommc.modularui.network;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.IFluidTankLong;
import com.cleanroommc.modularui.api.IItemStackLong;
import com.cleanroommc.modularui.utils.fluid.FluidTankLong;
import com.cleanroommc.modularui.utils.fluid.FluidTankLongDelegate;
import com.cleanroommc.modularui.utils.item.ItemStackLong;

import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.Nullable;

import static com.google.common.primitives.Ints.saturatedCast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class NetworkUtils {

    public static final Consumer<PacketBuffer> EMPTY_PACKET = buffer -> {
    };

    public static final boolean DEDICATED_CLIENT = FMLCommonHandler.instance().getSide().isClient();

    public static boolean isClient() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static boolean isDedicatedClient() {
        return DEDICATED_CLIENT;
    }

    public static boolean isClient(EntityPlayer player) {
        if (player == null) throw new NullPointerException("Can't get side of null player!");
        return player.worldObj == null ? player instanceof EntityPlayerSP : player.worldObj.isRemote;
    }

    public static void writeByteBuf(PacketBuffer writeTo, ByteBuf writeFrom) {
        writeTo.writeVarIntToBuffer(writeFrom.readableBytes());
        writeTo.writeBytes(writeFrom);
    }

    public static ByteBuf readByteBuf(PacketBuffer buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarIntFromBuffer());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        return copiedDataBuffer;
    }

    public static PacketBuffer readPacketBuffer(PacketBuffer buf) {
        return new PacketBuffer(readByteBuf(buf));
    }

    public static void writeItemStack(PacketBuffer buffer, ItemStack itemStack) {
        try {
            buffer.writeItemStackToBuffer(itemStack);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
        }
    }

    public static ItemStack readItemStack(PacketBuffer buffer) {
        try {
            return buffer.readItemStackFromBuffer();
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return null;
        }
    }

    public static void writeItemStackLong(PacketBuffer buffer, IItemStackLong itemStack) {
        try {
            NBTTagCompound nbt = new NBTTagCompound();
            if (itemStack == null) {
                nbt.setInteger("check", -1);
            } else {
                itemStack.writeToNBT(nbt);
                nbt.setInteger("check", 1);
            }
            buffer.writeNBTTagCompoundToBuffer(nbt);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
        }
    }

    public static IItemStackLong readItemStackLong(PacketBuffer buffer) {
        try {
            NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
            int check = nbt.getInteger("check");
            if (check < 0) return null;
            return ItemStackLong.loadFromNBT(nbt);
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return null;
        }
    }

    public static void writeFluidStack(PacketBuffer buffer, @Nullable FluidStack fluidStack) {
        if (fluidStack == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidStackTag = fluidStack.writeToNBT(new NBTTagCompound());
            try {
                buffer.writeNBTTagCompoundToBuffer(fluidStackTag);
            } catch (IOException e) {
                ModularUI.LOGGER.catching(e);
            }
        }
    }

    @Nullable
    public static FluidStack readFluidStack(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return null;
        }
        try {
            return FluidStack.loadFluidStackFromNBT(buffer.readNBTTagCompoundFromBuffer());
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return null;
        }
    }

    public static void writeFluidTank(PacketBuffer buffer, @Nullable IFluidTankLong tank) {
        if (tank == null) {
            buffer.writeBoolean(true);
        } else {
            buffer.writeBoolean(false);
            NBTTagCompound fluidStackTag = new NBTTagCompound();
            fluidStackTag.setLong("cap", tank.getCapacityLong());
            fluidStackTag.setLong("amt", tank.getFluidAmountLong());
            if (tank.getRealFluid() != null) {
                fluidStackTag.setString("flu", tank.getRealFluid().getName());
            }
            if (tank instanceof FluidTankLongDelegate) {
                fluidStackTag.setByte("type", (byte)1);
            } else {
                fluidStackTag.setByte("type", (byte)0);
            }
            try {
                buffer.writeNBTTagCompoundToBuffer(fluidStackTag);
            } catch (IOException e) {
                ModularUI.LOGGER.catching(e);
            }
        }
    }

    public static @Nonnull IFluidTankLong readFluidTank(PacketBuffer buffer) {
        if (buffer.readBoolean()) {
            return null;
        }
        try {
            NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
            byte type = nbt.getByte("type");
            Fluid fluid = FluidRegistry.getFluid(nbt.getString("flu"));
            long cap = nbt.getLong("cap");
            long amt = nbt.getLong("amt");
            IFluidTankLong tank;
            if (type == 1) {
                tank = new FluidTankLongDelegate(new FluidTank(fluid, saturatedCast(cap), saturatedCast(amt)));
            } else {
                tank = new FluidTankLong(fluid, cap, amt);
            }
            return tank;
        } catch (IOException e) {
            ModularUI.LOGGER.catching(e);
            return new FluidTankLong();
        }
    }

    public static void writeStringSafe(PacketBuffer buffer, String string) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, false);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, boolean crash) {
        writeStringSafe(buffer, string, Short.MAX_VALUE, crash);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, int maxBytes) {
        writeStringSafe(buffer, string, maxBytes, false);
    }

    public static void writeStringSafe(PacketBuffer buffer, @Nullable String string, int maxBytes, boolean crash) {
        maxBytes = Math.min(maxBytes, Short.MAX_VALUE);
        if (string == null) {
            buffer.writeVarIntToBuffer(Short.MAX_VALUE + 1);
            return;
        }
        byte[] bytesTest = string.getBytes(StandardCharsets.UTF_8);
        byte[] bytes;

        if (bytesTest.length > maxBytes) {
            if (crash) {
                throw new IllegalArgumentException("Max String size is " + maxBytes + ", but found " + bytesTest.length + " bytes for '" + string + "'!");
            }
            bytes = new byte[maxBytes];
            System.arraycopy(bytesTest, 0, bytes, 0, maxBytes);
            ModularUI.LOGGER.warn("Warning! Synced string exceeds max length!");
        } else {
            bytes = bytesTest;
        }
        buffer.writeVarIntToBuffer(bytes.length);
        buffer.writeBytes(bytes);
    }

    public static String readStringSafe(PacketBuffer buffer) {
        int length = buffer.readVarIntFromBuffer();
        if (length > Short.MAX_VALUE) {
            return null;
        }
        String s = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.readerIndex(buffer.readerIndex() + length);
        return s;
    }

    public static void writeEnumValue(PacketBuffer buffer, Enum<?> value) {
        buffer.writeVarIntToBuffer(value.ordinal());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T readEnumValue(PacketBuffer buffer, Class<T> enumClass) {
        return (T)((Enum<T>[])enumClass.getEnumConstants())[buffer.readVarIntFromBuffer()];
    }
}
