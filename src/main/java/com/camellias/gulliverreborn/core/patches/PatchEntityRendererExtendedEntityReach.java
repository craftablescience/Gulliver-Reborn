/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package com.camellias.gulliverreborn.core.patches;

import com.camellias.gulliverreborn.core.helper.ClassPatch;
import net.minecraftforge.fml.relauncher.Side;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PatchEntityRendererExtendedEntityReach extends ClassPatch {

    public PatchEntityRendererExtendedEntityReach() {
        super("net.minecraft.client.renderer.EntityRenderer");
    }

    @Override
    public void patch(ClassNode cn) {
        MethodNode mn = getMethodLazy(cn, "getMouseOver", "func_78473_a");
        MethodInsnNode checkExReach = getFirstMethodCall(mn,
                "net/minecraft/client/multiplayer/PlayerControllerMP",
                "extendedReach",
                "func_78749_i",
                "()Z");
        int overwrite = peekFirstInstructionAfter(mn, mn.instructions.indexOf(checkExReach),
                (a) -> a instanceof LdcInsnNode &&
                        ((LdcInsnNode) a).cst instanceof Number &&
                        Math.abs(((Number) ((LdcInsnNode) a).cst).doubleValue() - 6.0D) <= 0.01D);
        if (overwrite != -1) {
            AbstractInsnNode node = mn.instructions.get(overwrite);
            AbstractInsnNode prev = node.getPrevious();
            mn.instructions.remove(node);
            mn.instructions.insert(prev, new VarInsnNode(Opcodes.DLOAD, 8));
        }

        int afterApply = peekFirstInstructionAfter(mn, mn.instructions.indexOf(checkExReach),
                (a) -> a instanceof VarInsnNode &&
                        a.getOpcode() == Opcodes.ISTORE &&
                        ((VarInsnNode) a).var == 6);
        if (afterApply != -1) {
            mn.instructions.insert(mn.instructions.get(afterApply), new VarInsnNode(Opcodes.ISTORE, 6));
            mn.instructions.insert(mn.instructions.get(afterApply), new InsnNode(Opcodes.ICONST_0));
        }
    }
    @Override
    public boolean canExecuteForSide(Side side) {
        return side == Side.CLIENT;
    }
}
