package com.cleanroommc.modularui.core;

import com.cleanroommc.modularui.core.visitor.PacketByteBufferVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

@SuppressWarnings("unused")
public class ModularUITransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals(PacketByteBufferVisitor.PACKET_BUFFER_CLASS)) {
            ClassWriter classWriter = new ClassWriter(0);
            new ClassReader(basicClass).accept(new PacketByteBufferVisitor(classWriter), 0);
            ModularUICore.LOGGER.info("Applied {} ASM from ModularUI", transformedName);
            return classWriter.toByteArray();
        }
        return basicClass;
    }
}
