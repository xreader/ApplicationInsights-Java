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
class EnterExitMethodWrapper extends AdvancedAdapter {

    private InstrumentedClassType type;

    public EnterExitMethodWrapper(InstrumentedClassType type, int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        super(Opcodes.ASM5, methodVisitor, access, owner, methodName, desc);
        this.type = type;
    }

    @Override
    protected void byteCodeForMethodExit(int opcode) {

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

//        int boxedArgumentsIndex = 0;
//        int argumentIndex = isMethodStatic() ? 0 : 1;
//
//        TempArrayVar arrayOfArgumentsAfterBoxing;
//        arrayOfArgumentsAfterBoxing = createArray(numberOfArguments());

        // Fill the array with method parameters after boxing the primitive ones.
//        for (Type argumentType : getArgumentTypes()) {
//            setBoxedValueIntoArray(arrayOfArgumentsAfterBoxing, boxedArgumentsIndex, argumentType, argumentIndex);
//
//            boxedArgumentsIndex++;
//            if (ByteCodeUtils.isLargeType(argumentType)) {
//                argumentIndex += 2;
//            } else {
//                ++argumentIndex;
//            }
//        }

        activateEnumMethod(
                ImplementationsCoordinator.class,
                "onDefaultMethodEnter",
//                ImplementationsCoordinator.onMethodEnterName(type),
                "(Ljava/lang/String;)V",
//                "(Ljava/lang/String;[Ljava/lang/Object;)V",
                getMethodName());
//        ,
//                arrayOfArgumentsAfterBoxing);
    }

    private void setBoxedValueIntoArray(TempArrayVar arrayOfArgumentsAfterBoxing, int boxedArgumentsIndex, Type argumentType, int argumentIndex) {
        prepareArrayEntry(arrayOfArgumentsAfterBoxing.tempVarIndex, boxedArgumentsIndex);
        boxVariable(argumentType, argumentIndex);
        storeTopStackValueIntoArray();
    }
}
