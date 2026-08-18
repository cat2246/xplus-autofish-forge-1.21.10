package com.wudji.xplusautofish.gui;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AutoFishConfigScreenRenderTest {
    @Test
    void renderDoesNotInvokeScreenRenderBackground() throws IOException {
        String classResource = "/" + AutoFishConfigScreen.class.getName().replace('.', '/') + ".class";
        String screenClass = "net/minecraft/client/gui/screens/Screen";
        String configScreenClass = AutoFishConfigScreen.class.getName().replace('.', '/');
        boolean[] invokesScreenRenderBackground = {false};

        try (InputStream bytecode = AutoFishConfigScreen.class.getResourceAsStream(classResource)) {
            assertNotNull(bytecode, "compiled AutoFishConfigScreen bytecode must be available");
            new ClassReader(bytecode).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    MethodVisitor delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                    if (!name.equals("render")
                            || !descriptor.equals("(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")) {
                        return delegate;
                    }
                    return new MethodVisitor(Opcodes.ASM9, delegate) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName,
                                                    String methodDescriptor, boolean isInterface) {
                            if (opcode == Opcodes.INVOKEVIRTUAL
                                    && (owner.equals(screenClass) || owner.equals(configScreenClass))
                                    && methodName.equals("renderBackground")
                                    && methodDescriptor.equals("(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")) {
                                invokesScreenRenderBackground[0] = true;
                            }
                            super.visitMethodInsn(opcode, owner, methodName, methodDescriptor, isInterface);
                        }
                    };
                }
            }, 0);
        }

        assertFalse(invokesScreenRenderBackground[0],
                "AutoFishConfigScreen.render must not invoke Screen.renderBackground; Screen.renderWithTooltipAndSubtitles already does");
    }
}
