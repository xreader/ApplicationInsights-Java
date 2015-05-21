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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;

import com.microsoft.applicationinsights.internal.agent.config.AgentConfiguration;
import com.microsoft.applicationinsights.internal.agent.config.AgentConfigurationBuilderFactory;
import com.microsoft.applicationinsights.internal.coresync.impl.ImplementationsCoordinator;
import com.microsoft.applicationinsights.internal.scanner.ClassesScanner;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * The class is responsible for finding needed classes
 *
 * Created by gupele on 5/11/2015.
 */
public final class CodeInjector implements ClassFileTransformer {

    private enum InstrumentationType {
        NOT_NEEDED,
        REGULAR,
        THREAD
    }

    private static class NeededInstrumentation {
        public final String threadKey;
        public final InstrumentationType instrumentedType;
        public final ClassInstrumentationData classInstrumentationData;

        private NeededInstrumentation(InstrumentationType instrumentedType, ClassInstrumentationData classInstrumentationData) {
            this(instrumentedType, classInstrumentationData, null);
        }

        private NeededInstrumentation(InstrumentationType instrumentedType, ClassInstrumentationData classInstrumentationData, String threadKey) {
            this.instrumentedType = instrumentedType;
            this.classInstrumentationData = classInstrumentationData;
            this.threadKey = threadKey;
        }
    }

    private Map<String, ClassInstrumentationData> classNameThatInitializes;
    private Map<String, ClassInstrumentationData> interfaces;
    private ClassNamesProvider classNamesProvider = new DefaultClassNamesProvider();

    public CodeInjector(Instrumentation inst, String agentJarLocation) {

        try {
            ClassesScanner scanner = new ClassesScanner();
            List<String> result = scanner.scanForClasses("com.mysql");
            if (result != null) {
                System.out.println("result");
                for (String r : result) {
                    System.out.println(r);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            AgentConfiguration agentConfiguration = new AgentConfigurationBuilderFactory().createBuilder(null).parseConfigurationFile(agentJarLocation);
            classNamesProvider.setConfiguration(agentConfiguration);

            this.interfaces = classNamesProvider.getInterfaces();
            this.classNameThatInitializes = classNamesProvider.getClassesAndMethods();
            inst.addTransformer(this);

            System.out.println("Agent is up");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.out.println(String.format("Agent is NOT activated: failed to initialize CodeInjector: '%s'", throwable.getMessage()));
        }
    }

    public byte[] transform(
            ClassLoader loader,
            String className,
            Class classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] originalBuffer) throws IllegalClassFormatException {

        if (isForbidden(className)) {
            return originalBuffer;
        }

        NeededInstrumentation neededInstrumentation = getRelevantMethods(loader, className, originalBuffer);
        if (neededInstrumentation != null) {
            try {
                switch (neededInstrumentation.instrumentedType) {
                    case REGULAR:
                        System.out.println(String.format("instrumenting '%s'", className));
                        return getTransformedBytes(originalBuffer, neededInstrumentation.classInstrumentationData, className);

                    case THREAD:
                        System.out.println(String.format("instrumenting thread '%s'", className));
                        return getTransformedBytesForNewThreads(originalBuffer, neededInstrumentation.classInstrumentationData, neededInstrumentation.threadKey, className);

                    default:
                        return originalBuffer;
                }
            } catch (Throwable throwable) {
                System.err.println(String.format("Failed to instrument '%s', exception: '%s': ", neededInstrumentation, throwable.getMessage()));
            }
        }

        byte[] byteCode = originalBuffer;

        return byteCode;
    }

    private boolean isForbidden(String className) {
        HashSet<String> forbidden = classNamesProvider.getForbiddenPaths();
        for (String f : forbidden) {
            if (className.startsWith(f)) {
                return true;
            }
        }
        return false;
    }

    private NeededInstrumentation getRelevantMethods(ClassLoader classLoader, String className, byte[] originalBuffer) {

        ClassInstrumentationData classInstrumentationData = classNameThatInitializes.remove(className);

        if (classInstrumentationData != null) {
            return new NeededInstrumentation(InstrumentationType.REGULAR, classInstrumentationData);
        } else {
            return getMethodsOfInterfaces(className, originalBuffer);
        }
    }

    private NeededInstrumentation getMethodsOfInterfaces(String className, byte[] originalBuffer) {

        try {
            if (className == null) {
                return null;
            }

            ClassReader cr = new ClassReader(originalBuffer);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            SuperVerifier finder = new SuperVerifier(interfaces.keySet(), cw);
            cr.accept(finder, ClassReader.EXPAND_FRAMES);

            ClassInstrumentationData result = null;
            for (String i : finder.getFound()) {
                ClassInstrumentationData data = interfaces.get(i);
                if (result == null) {
                    result = new ClassInstrumentationData(data.classType);
                }
                System.out.println("interface : " + i);
                result.addMethods(data.methodNamesOnly);
                result.addMethods(data.methods);
            }
            return new NeededInstrumentation(InstrumentationType.REGULAR, result);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getTransformedBytes(byte[] originalBuffer, ClassInstrumentationData instrumentationData, String className) {
        ClassReader cr = new ClassReader(originalBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        EnterExitClassVisitor mcw = new EnterExitClassVisitor(instrumentationData, cw, className);
        cr.accept(mcw, ClassReader.EXPAND_FRAMES);
        byte[] b2 = cw.toByteArray();
        return b2;
    }

    private byte[] getTransformedBytesForNewThreads(byte[] originalBuffer, ClassInstrumentationData instrumentationData, String threadKey, String className) {
        ClassReader cr = new ClassReader(originalBuffer);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        EnterExitClassVisitor mcw = new EnterExitClassVisitor(threadKey, instrumentationData, cw, className);
        cr.accept(mcw, ClassReader.EXPAND_FRAMES);
        byte[] b2 = cw.toByteArray();
        return b2;
    }

    private String getRegisteredClassLoader(ClassLoader classLoader) {
        for (ImplementationsCoordinator.RegistrationData registrationData : ImplementationsCoordinator.INSTANCE.getRegistered()) {
            ClassLoader checkedClassLoader = registrationData.classLoader;
            while (checkedClassLoader != null) {
                if (checkedClassLoader == classLoader) {
                    return registrationData.key;
                }
            }
        }

        return null;
    }
}
