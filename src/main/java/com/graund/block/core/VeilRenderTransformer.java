package com.graund.block.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Inserts the Veil depth pass immediately before RenderGlobal draws the
 * SOLID terrain layer in EntityRenderer.renderWorldPass().
 */
public class VeilRenderTransformer implements IClassTransformer {
    private static final String TARGET_CLASS =
            "net.minecraft.client.renderer.EntityRenderer";
    private static final String HOOK_CLASS =
            "com/graund/block/client/render/MaskDepthPass";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET_CLASS.equals(transformedName)) {
            return basicClass;
        }

        ClassReader reader = new ClassReader(basicClass);
        ClassNode classNode = new ClassNode(Opcodes.ASM5);
        reader.accept(classNode, 0);

        for (MethodNode method : classNode.methods) {
            boolean targetMethod = ("renderWorldPass".equals(method.name)
                    || "func_175068_a".equals(method.name))
                    && "(IFJ)V".equals(method.desc);

            if (!targetMethod) {
                continue;
            }

            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null;
                 instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }

                MethodInsnNode invocation = (MethodInsnNode) instruction;
                boolean renderBlockLayer = "renderBlockLayer".equals(invocation.name)
                        || "func_174977_a".equals(invocation.name);

                if (renderBlockLayer) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.FLOAD, 2));
                    hook.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HOOK_CLASS,
                            "render",
                            "(F)V",
                            false
                    ));
                    method.instructions.insertBefore(instruction, hook);

                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
                    classNode.accept(writer);
                    System.out.println("[Veil Block] Installed client depth-pass hook.");
                    return writer.toByteArray();
                }
            }

            System.err.println("[Veil Block] Could not find the terrain render call in EntityRenderer.");
            return basicClass;
        }

        System.err.println("[Veil Block] Could not find EntityRenderer.renderWorldPass().");
        return basicClass;
    }
}
