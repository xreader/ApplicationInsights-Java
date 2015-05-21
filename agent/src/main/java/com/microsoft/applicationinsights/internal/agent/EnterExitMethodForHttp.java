package com.microsoft.applicationinsights.internal.agent;

import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;
import com.microsoft.applicationinsights.internal.coresync.impl.ImplementationsCoordinator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.net.URL;

/**
 * Created by gupele on 5/20/2015.
 */
final class EnterExitMethodForHttp extends EnterExitMethodWrapper {
    private final String owner;

    public EnterExitMethodForHttp(InstrumentedClassType type, int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        super(type, access, desc, owner, methodName, methodVisitor);
        this.owner = owner;
        System.out.println("EnterExitMethodForHttp " + owner);
    }

    @Override
    protected void onMethodEnter() {
        int urlLocalIndex = this.newLocal(Type.getType(URL.class));

        // "sun/net/www/protocol/http/HttpURLConnection"
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, owner, "getURL", "()Ljava/net/URL;", false);
        mv.visitVarInsn(ASTORE, urlLocalIndex);

//        mv.visitVarInsn(ALOAD, urlLocalIndex);
//
//        Label l0 = new Label();
//        mv.visitJumpInsn(IFNULL, l0);
//        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitVarInsn(Opcodes.ALOAD, urlLocalIndex);
//        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
//
//        mv.visitLabel(l0);
//        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/net/URL"}, 0, null);

        mv.visitVarInsn(Opcodes.ALOAD, urlLocalIndex);

        activateEnumMethod(
                ImplementationsCoordinator.class,
                "onMethodEnterURL",
                "(Ljava/lang/String;Ljava/net/URL;)V",
                getMethodName(),
                duplicateTopStackToTempVariable(Type.getType(URL.class)));
    }
}
