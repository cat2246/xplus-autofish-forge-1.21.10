package com.wudji.xplusautofish.gui;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigListLayoutTest {
    @Test
    void constructorPassesListHeightAndTopToSelectionList() throws IOException {
        String classResource = "/" + AutoFishConfigScreen.class.getName().replace('.', '/')
                + "$ConfigList.class";
        String selectionListClass = "net/minecraft/client/gui/components/ContainerObjectSelectionList";
        boolean[] foundConstructor = {false};
        boolean[] passesCorrectGeometry = {false};

        try (InputStream bytecode = AutoFishConfigScreen.class.getResourceAsStream(classResource)) {
            assertNotNull(bytecode, "compiled ConfigList bytecode must be available");
            new ClassReader(bytecode).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                  String signature, String[] exceptions) {
                    MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (!name.equals("<init>")
                            || !descriptor.equals("(Lcom/wudji/xplusautofish/gui/AutoFishConfigScreen;"
                            + "Lnet/minecraft/client/Minecraft;III)V")) {
                        return delegate;
                    }
                    foundConstructor[0] = true;
                    List<String> instructions = new ArrayList<>();
                    return new MethodVisitor(Opcodes.ASM9, delegate) {
                        @Override
                        public void visitVarInsn(int opcode, int var) {
                            instructions.add("var:" + opcode + ":" + var);
                            super.visitVarInsn(opcode, var);
                        }

                        @Override
                        public void visitIntInsn(int opcode, int operand) {
                            instructions.add("int:" + opcode + ":" + operand);
                            super.visitIntInsn(opcode, operand);
                        }

                        @Override
                        public void visitInsn(int opcode) {
                            instructions.add("insn:" + opcode);
                            super.visitInsn(opcode);
                        }

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName,
                                                    String methodDescriptor, boolean isInterface) {
                            if (opcode == Opcodes.INVOKESPECIAL
                                    && owner.equals(selectionListClass)
                                        && methodName.equals("<init>")
                                        && methodDescriptor.equals("(Lnet/minecraft/client/Minecraft;IIII)V")) {
                                List<String> expected = List.of(
                                        "var:" + Opcodes.ALOAD + ":0",
                                        "var:" + Opcodes.ALOAD + ":2",
                                        "var:" + Opcodes.ILOAD + ":3",
                                        "int:" + Opcodes.BIPUSH + ":24",
                                        "insn:" + Opcodes.ISUB,
                                        "var:" + Opcodes.ILOAD + ":5",
                                        "var:" + Opcodes.ILOAD + ":4",
                                        "insn:" + Opcodes.ISUB,
                                        "var:" + Opcodes.ILOAD + ":4",
                                        "int:" + Opcodes.BIPUSH + ":32");
                                passesCorrectGeometry[0] = instructions.size() >= expected.size()
                                        && instructions.subList(instructions.size() - expected.size(), instructions.size())
                                        .equals(expected);
                            }
                            super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                        }
                    };
                }
            }, 0);
        }

        assertTrue(foundConstructor[0], "compiled ConfigList constructor must be visited");
        assertTrue(passesCorrectGeometry[0],
                "ConfigList must pass width-24, bottom-top, top, and item height 32 to ContainerObjectSelectionList");
    }
}
