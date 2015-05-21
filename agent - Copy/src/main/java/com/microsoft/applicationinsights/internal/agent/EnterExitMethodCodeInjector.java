/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.internal.agent;

import com.microsoft.applicationinsights.internal.coresync.AgentNotificationsHandler;
import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;
import com.microsoft.applicationinsights.internal.coresync.impl.ImplementationsCoordinator;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is responsible for finding where the method starts and ends.
 *
 * When the method starts, the class will inject byte code that will call our code with the class name
 * method name and the arguments, and when the method ends will call again with the method name and the result
 * or exception if there is one, the class will make sure that the original code's behavior is not changed
 *
 * Created by gupele on 5/11/2015.
 */
final class EnterExitMethodCodeInjector extends AdvancedAdapter {

    private final boolean ownerown;
    private InstrumentedClassType type;
    private String keyInjection;

    public EnterExitMethodCodeInjector(String keyInjection, InstrumentedClassType type, int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        super(Opcodes.ASM5, methodVisitor, access, owner, methodName, desc);
        this.type = type;
        this.keyInjection = keyInjection;



        this.ownerown = owner.endsWith("sun/net/www/protocol/http/HttpURLConnection");
        if (ownerown) {
            System.out.println("ownerown");
        }
    }

    @Override
    protected void byteCodeForMethodExit(int opcode) {
        if (keyInjection != null) {
            return;
        }

        Object[] args = null;
        switch (translateExitCode(opcode)) {
            case EXIT_WITH_EXCEPTION:
                args = new Object[] { duplicateTopStackToTempVariable(Type.getType(Throwable.class)), getMethodName() };
                break;

            case EXIT_WITH_RETURN_VALUE:
                args = new Object[] { getMethodName(), duplicateTopStackToTempVariable(getReturnType()) };
                break;

            case EXIT_VOID:
                args = new Object[] { getMethodName() };
                break;

            default:
                break;
        }

        if (args != null) {
            activateEnumMethod(ImplementationsCoordinator.class, ImplementationsCoordinator.onMethodEndName(), ImplementationsCoordinator.onMethodEndSignature(opcode), args);
        }
    }

    @Override
    protected void onMethodEnter() {
        if (keyInjection != null) {
            activateEnumMethod(
                    ImplementationsCoordinator.class,
                    "onNewThread",
                    "(Ljava/lang/String;)V",
                    keyInjection);
            return;
        }

        // IllegalAccessError
        int urlLocalIndex = -1;
        if (ownerown) {
            urlLocalIndex = this.newLocal(Type.getType(URL.class));

            mv.visitVarInsn(ALOAD, 0);
//            mv.visitFieldInsn(GETFIELD, "sun/net/www/protocol/http/HttpURLConnection", "this$0", "Lsun/net/www/protocol/http/HttpURLConnection;");
//            mv.visitFieldInsn(GETFIELD, "sun/net/www/protocol/http/HttpURLConnection$HttpInputStream", "this$0", "Lsun/net/www/protocol/http/HttpURLConnection;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/net/www/protocol/http/HttpURLConnection", "getURL", "()Ljava/net/URL;", false);
            mv.visitVarInsn(ASTORE, urlLocalIndex);

            mv.visitVarInsn(ALOAD, urlLocalIndex);

            Label l0 = new Label();
            mv.visitJumpInsn(IFNULL, l0);
            super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitVarInsn(Opcodes.ALOAD, urlLocalIndex);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);

            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/net/URL"}, 0, null);
        }

        int boxedArgumentsIndex = 0;
        int argumentIndex = isMethodStatic() ? 0 : 1;

        int n = numberOfArguments();
        if (ownerown) {
            n += 1;
        }

        TempArrayVar arrayOfArgumentsAfterBoxing;
        if (ownerown) {
            arrayOfArgumentsAfterBoxing = createArray(n, 1);

            System.out.println("enter method " + urlLocalIndex + " " + argumentIndex + " " + n + " " + arrayOfArgumentsAfterBoxing.tempVarIndex);
            mv.visitVarInsn(Opcodes.ALOAD, arrayOfArgumentsAfterBoxing.tempVarIndex);
            mv.visitIntInsn(Opcodes.BIPUSH, boxedArgumentsIndex);
            mv.visitVarInsn(Opcodes.ALOAD, urlLocalIndex);
            mv.visitInsn(Opcodes.AASTORE);
            ++boxedArgumentsIndex;
        } else {
            arrayOfArgumentsAfterBoxing = createArray(n);
        }

        // Fill the array with method parameters after boxing the primitive ones.
        for (Type argumentType : getArgumentTypes()) {
            setBoxedValueIntoArray(arrayOfArgumentsAfterBoxing, boxedArgumentsIndex, argumentType, argumentIndex);

            boxedArgumentsIndex++;
            if (ByteCodeUtils.isLargeType(argumentType)) {
                argumentIndex += 2;
            } else {
                ++argumentIndex;
            }
        }

        activateEnumMethod(
                ImplementationsCoordinator.class,
                ImplementationsCoordinator.onMethodEnterName(type),
                "(Ljava/lang/String;[Ljava/lang/Object;)V",
                getMethodName(),
                arrayOfArgumentsAfterBoxing);
    }

    private void setBoxedValueIntoArray(TempArrayVar arrayOfArgumentsAfterBoxing, int boxedArgumentsIndex, Type argumentType, int argumentIndex) {
        prepareArrayEntry(arrayOfArgumentsAfterBoxing.tempVarIndex, boxedArgumentsIndex);
        boxVariable(argumentType, argumentIndex);
        storeTopStackValueIntoArray();
    }
}
