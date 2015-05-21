package com.microsoft.applicationinsights.internal.agent;

import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;
import com.microsoft.applicationinsights.internal.coresync.impl.ImplementationsCoordinator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.net.URL;

/**
 * Created by gupele on 5/20/2015.
 */
public class EnterExitMethodForSqlStatement extends EnterExitMethodWrapper {
    public EnterExitMethodForSqlStatement(InstrumentedClassType type, int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        super(type, access, desc, owner, methodName, methodVisitor);
    }

    @Override
    protected void onMethodEnter() {

        String internalName = Type.getInternalName(ImplementationsCoordinator.class);
        super.visitFieldInsn(Opcodes.GETSTATIC, internalName, "INSTANCE", "L" + internalName + ";");

        mv.visitLdcInsn(getMethodName());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalName, "onMethodEnterSqlStatement", "(Ljava/lang/String;Ljava/sql/Statement;Ljava/lang/String;)V", false);
    }
}
