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

package com.microsoft.applicationinsights.agent.internal.agent.instrumentor;

import com.microsoft.applicationinsights.agent.internal.agent.ClassInstrumentationData;
import com.microsoft.applicationinsights.agent.internal.agent.MethodInstrumentationDecision;
import org.objectweb.asm.*;

/**
 * The class is responsible for identifying public methods on non-interface classes.
 * When a method is found the class will call the {@link com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultMethodInstrumentor}
 *
 * Created by gupele on 5/11/2015.
 */
public class DefaultClassInstrumentor extends ClassVisitor {
    private boolean isInterface;
    private final ClassInstrumentationData instrumentationData;

    protected final MethodInstrumentorsFactory factory;
    protected String owner;

    public DefaultClassInstrumentor(MethodInstrumentorsFactory factory, ClassInstrumentationData instrumentationData, ClassWriter cv) {
        super(Opcodes.ASM5, cv);

        owner = instrumentationData.getClassName();
        this.instrumentationData = instrumentationData;
        this.factory = factory;
    }

    public DefaultClassInstrumentor(ClassInstrumentationData instrumentationData, ClassWriter cw) {
        super(Opcodes.ASM5, cw);

        owner = instrumentationData.getClassName();
        this.instrumentationData = instrumentationData;
        this.factory = null;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        isInterface = ByteCodeUtils.isInterface(access);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (owner.indexOf("AsyncCall") != -1) {
            System.out.println("asyncLL" + name + " " + desc);
        }
        MethodVisitor originalMV = super.visitMethod(access, name, desc, signature, exceptions);

        if (isInterface || originalMV == null || ByteCodeUtils.isConstructor(name) || ByteCodeUtils.isPrivate(access)) {
            return originalMV;
        }

        MethodInstrumentationDecision decision = instrumentationData.getDecisionForMethod(name, desc);
        if (decision == null) {
            return originalMV;
        }

        return getMethodVisitor(decision, access, name, desc, originalMV);
    }

    protected MethodVisitor getMethodVisitor(MethodInstrumentationDecision decision, int access, String name, String desc, MethodVisitor originalMV) {
        if (factory == null) {
//            instrumentationData.get
        }
        return factory.getMethodVisitor(decision, access, desc, owner, name, originalMV, null);
    }
}
