package com.cleanroommc.modularui.core.visitor;

import com.cleanroommc.modularui.core.ModularUICore;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Write item stack with var int stack size instead of byte stack size
 */
public class PacketByteBufferVisitor extends ClassVisitor implements Opcodes {

    public static final String PACKET_BUFFER_CLASS = "net.minecraft.network.PacketBuffer";
    // Due to the bug with Mixin, obfuscated names are given instead of SRG names,
    // even if SortingIndex is set to higher value
    private static final String WRITE_ITEMSTACK_METHOD = ModularUICore.isDevEnv() ? "writeItemStackToBuffer" : "a";
    private static final String READ_ITEMSTACK_METHOD = ModularUICore.isDevEnv() ? "readItemStackFromBuffer" : "c";
    private static final String WRITE_ITEMSTACK_DESC = ModularUICore.isDevEnv() ? "(Lnet/minecraft/item/ItemStack;)V" : "(Ladd;)V";
    private static final String READ_ITEMSTACK_DESC = ModularUICore.isDevEnv() ? "()Lnet/minecraft/item/ItemStack;" : "()Ladd;";
    private static final String WRITE_VAR_INT_METHOD = ModularUICore.isDevEnv() ? "writeVarIntToBuffer" : "func_150787_b";
    private static final String READ_VAR_INT_METHOD = ModularUICore.isDevEnv() ? "readVarIntFromBuffer" : "func_150792_a";

    public PacketByteBufferVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((WRITE_ITEMSTACK_METHOD.equals(name) && WRITE_ITEMSTACK_DESC.equals(desc))
            || (READ_ITEMSTACK_METHOD.equals(name) && READ_ITEMSTACK_DESC.equals(desc))) {
            ModularUICore.LOGGER.debug("Start patching " + name);
            return new ReadWriteItemStackVisitor(mv);
        }
        return mv;
    }

    private static class ReadWriteItemStackVisitor extends MethodVisitor {

        private boolean skipPop;

        public ReadWriteItemStackVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitInsn(int opcode) {
            if (skipPop && opcode == POP) {
                // writeByte returns ByteBuf, while writeVarIntToBuffer returns void
                ModularUICore.LOGGER.debug("Skipped POP");
                skipPop = false;
                return;
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if ("writeByte".equals(name)) {
                name = WRITE_VAR_INT_METHOD;
                desc = "(I)V";
                ModularUICore.LOGGER.debug("Patched writeByte");
                skipPop = true;
            } else if ("readByte".equals(name)) {
                name = READ_VAR_INT_METHOD;
                desc = "()I";
                ModularUICore.LOGGER.debug("Patched readByte");
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
