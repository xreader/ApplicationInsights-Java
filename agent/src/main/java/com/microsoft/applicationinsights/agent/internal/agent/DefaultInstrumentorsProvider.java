/*
 * AppInsights-Java
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

package com.microsoft.applicationinsights.agent.internal.agent;

import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultClassInstrumentor;
import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultMethodInstrumentor;
import com.microsoft.applicationinsights.agent.internal.common.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;

/**
 * Created by gupele on 7/26/2015.
 */
final class DefaultInstrumentorsProvider implements InstrumentorsProvider {
    private final HashMap<String, MethodVisitorFactory> classAndMethodToMethodVisitor = new HashMap<String, MethodVisitorFactory>();
    private final HashMap<String, DefaultClassInstrumentorFactory> classesToClassVisitors = new HashMap<String, DefaultClassInstrumentorFactory>();

    @Override
    public void addClassInstrumentor(String className, DefaultClassInstrumentorFactory factory) {
        if (StringUtils.isNullOrEmpty(className)) {
            return;
        }
        if (factory == null) {
            return;
        }

        classesToClassVisitors.put(className, factory);
    }

    @Override
    public void addMethodVisitorFactory(String className, String methodName, String methodSignature, MethodVisitorFactory factory) {
        if (StringUtils.isNullOrEmpty(className)) {
            return;
        }
        if (factory == null) {
            return;
        }

        classAndMethodToMethodVisitor.put(glueClassNameToMethodName(className, methodName, methodSignature), factory);
    }

    @Override
    public DefaultClassInstrumentor getClassInstrumentor(ClassInstrumentationData instrumentationData, ClassWriter cw) {
        DefaultClassInstrumentorFactory factory = classesToClassVisitors.get(instrumentationData.getClassName());
        if (factory == null) {
            return new DefaultClassInstrumentor(instrumentationData, cw);
        }

        return factory.create(instrumentationData, cw);
    }

    @Override
    public MethodVisitor getMethodVisitor(String className, String methodName, String methodSignature, int access, MethodInstrumentationDecision decision, MethodVisitor originalMV) {
        MethodVisitorFactory factory = classAndMethodToMethodVisitor.get(glueClassNameToMethodName(className, methodName,methodSignature));
        if (factory == null) {
            return new DefaultMethodInstrumentor(decision, access, methodSignature, className, methodName, originalMV);
        }

        return factory.create();
    }

    private static String glueClassNameToMethodName(String className, String methodName, String signature) {
        StringBuilder sb = new StringBuilder(className);
        sb.append('.')
                .append(methodName)
                .append('.')
                .append(signature);
        return sb.toString();
    }
}
