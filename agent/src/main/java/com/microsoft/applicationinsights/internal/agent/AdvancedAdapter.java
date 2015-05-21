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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Created by gupele on 5/11/2015.
 */
abstract class AdvancedAdapter extends AdviceAdapter {

    protected enum ExitStatus {
        EXIT_WITH_EXCEPTION,
        EXIT_WITH_RETURN_VALUE,
        EXIT_VOID,
        UNKNOWN
    }

    protected static class TempVar {
        public final int tempVarIndex;

        public TempVar(int tempVarIndex) {
            this.tempVarIndex = tempVarIndex;
        }
    }

    protected static class TempArrayVar {
        public final int tempVarIndex;

        public TempArrayVar(int tempVarIndex) {
            this.tempVarIndex = tempVarIndex;
        }
    }

    private String methodName;
    private String desc;

    private Type[] argumentTypes;
    private boolean isStatic;
    private int firstEmptyIndexForLocalVariable;

    private Label startTryFinallyBlock = new Label();
    private Label endTryFinallyBlock = new Label();

    @Override
    public void visitCode() {
        super.visitCode();
        mark(startTryFinallyBlock);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        visitTryCatchBlock(startTryFinallyBlock, endTryFinallyBlock, endTryFinallyBlock, null);
        mark(endTryFinallyBlock);

        byteCodeForMethodExit(Opcodes.ATHROW);

        mv.visitInsn(Opcodes.ATHROW);
        mv.visitMaxs(maxStack, maxLocals);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode != Opcodes.ATHROW) {
            byteCodeForMethodExit(opcode);
        }
    }

    @Override
    public void visitMethodInsn(int opcode,String owner,String name,String desc, boolean isMethodOwnerAnInterface) {
        super.visitMethodInsn(opcode, owner, name, desc, isMethodOwnerAnInterface);
    }

    protected AdvancedAdapter(int i, MethodVisitor methodVisitor, int access, String owner, String methodName, String desc) {
        super(i, methodVisitor, access, methodName, desc);
        this.desc = desc;
        this.methodName = owner + "." + methodName;

        argumentTypes = Type.getArgumentTypes(desc);
        firstEmptyIndexForLocalVariable = argumentTypes.length;
        for (Type tp : argumentTypes) {
            if (tp.equals(Type.LONG_TYPE) || tp.equals(Type.DOUBLE_TYPE)) {
                ++firstEmptyIndexForLocalVariable;
            }
        }

        isStatic = ByteCodeUtils.isStatic(access);
        if (!isStatic) {
            ++firstEmptyIndexForLocalVariable;
        }
    }

    protected boolean isMethodStatic() {
        return isStatic;
    }

    protected int numberOfArguments() {
        return argumentTypes.length;
    }

    protected Type[] getArgumentTypes() {
        return argumentTypes;
    }

    protected void duplicateTop(Type type) {
        if (type.getSize() == 2) {
            dup2();
        } else {
            dup();
        }
    }

    protected void activateEnumMethod(Class<?> clazz, String methodName, String methodSignature, Object... args) {
        String internalName = Type.getInternalName(clazz);
        super.visitFieldInsn(Opcodes.GETSTATIC, internalName, "INSTANCE", "L" + internalName + ";");
        for (Object arg : args) {
            if (arg instanceof TempVar) {
                loadLocal(((TempVar) arg).tempVarIndex);
            } else if (arg instanceof TempArrayVar) {
                super.visitVarInsn(Opcodes.ALOAD, ((TempArrayVar) arg).tempVarIndex);
            } else {
                super.visitLdcInsn(arg);
            }
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalName, methodName, methodSignature, false);
    }

    protected void printToSystemOut(String message) {
        super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        super.visitLdcInsn(message);
        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    protected void printToSystemOut(Object... objects) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects) {
            sb.append(object.toString());
        }
        printToSystemOut(sb.toString());
    }

    protected TempVar duplicateTopStackToTempVariable(Type typeOfTopElementInStack) {
        duplicateTop(typeOfTopElementInStack);
        int tempVarIndex = newLocal(typeOfTopElementInStack);
        storeLocal(tempVarIndex, typeOfTopElementInStack);

        return new TempVar(tempVarIndex);
    }

    protected String getMethodName() {
        return methodName;
    }

    protected Type getReturnType() {
        return Type.getReturnType(desc);
    }

    protected TempArrayVar createArray(int length, int extraArgs) {
        System.out.println(" length " + length + " " + extraArgs);
        firstEmptyIndexForLocalVariable += extraArgs;
        return createArray(length);
    }

    protected TempArrayVar createArray(int length) {
        mv.visitIntInsn(Opcodes.BIPUSH, length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        mv.visitVarInsn(Opcodes.ASTORE, firstEmptyIndexForLocalVariable);

        return new TempArrayVar(firstEmptyIndexForLocalVariable);
    }

    protected void prepareArrayEntry(int arrayIndex, int entryIndex) {
        mv.visitVarInsn(Opcodes.ALOAD, arrayIndex);
        mv.visitIntInsn(Opcodes.BIPUSH, entryIndex);
    }

    protected void storeTopStackValueIntoArray() {
        mv.visitInsn(Opcodes.AASTORE);
    }

    protected void boxVariable(Type argumentType, int argumentIndex) {
        if (argumentType.equals(Type.BOOLEAN_TYPE)) {
            mv.visitVarInsn(Opcodes.ILOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        }
        else if (argumentType.equals(Type.BYTE_TYPE)) {
            mv.visitVarInsn(Opcodes.ILOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        }
        else if (argumentType.equals(Type.CHAR_TYPE)) {
            mv.visitVarInsn(Opcodes.ILOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        }
        else if (argumentType.equals(Type.SHORT_TYPE)) {
            mv.visitVarInsn(Opcodes.ILOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        }
        else if (argumentType.equals(Type.INT_TYPE)) {
            mv.visitVarInsn(Opcodes.ILOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        }
        else if (argumentType.equals(Type.LONG_TYPE)) {
            mv.visitVarInsn(Opcodes.LLOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        }
        else if (argumentType.equals(Type.FLOAT_TYPE)) {
            mv.visitVarInsn(Opcodes.FLOAD, argumentIndex);
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "valueOf", "(F)Ljava/lang/Long;", false);
        }
        else if (argumentType.equals(Type.DOUBLE_TYPE)) {
            mv.visitVarInsn(Opcodes.DLOAD, argumentIndex);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
        else {
            mv.visitVarInsn(Opcodes.ALOAD, argumentIndex);
        }
    }

    protected ExitStatus translateExitCode(int opcode) {
        switch (opcode) {
            case Opcodes.ATHROW:
                return ExitStatus.EXIT_WITH_EXCEPTION;

            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
            case Opcodes.ARETURN:
                return ExitStatus.EXIT_WITH_RETURN_VALUE;

            case Opcodes.RETURN:
                return ExitStatus.EXIT_VOID;

            default:
                return ExitStatus.UNKNOWN;
        }
    }

    protected abstract void byteCodeForMethodExit(int opcode);

}
