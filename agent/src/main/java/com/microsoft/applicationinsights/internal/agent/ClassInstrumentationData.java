package com.microsoft.applicationinsights.internal.agent;

import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by gupele on 5/19/2015.
 */
public final class ClassInstrumentationData {
    public final InstrumentedClassType classType;

    // Methods that will be instrumented only by their names
    public final HashSet<String> methodNamesOnly;

    // Methods that will be instrumented by exact match of their name and signature
    public final ArrayList<MethodAndSignature> methods;

    public ClassInstrumentationData(InstrumentedClassType classType) {
        this(classType, new ArrayList<MethodAndSignature>(), new HashSet<String>());
    }

    public ClassInstrumentationData(InstrumentedClassType classType, ArrayList<MethodAndSignature> methods, HashSet<String> methodNamesOnly) {
        this.classType = classType;
        this.methods = methods;
        this.methodNamesOnly = methodNamesOnly;
    }

    public void addMethods(HashSet<String> methodNamesOnly) {
        if (methodNamesOnly != null) {
            this.methodNamesOnly.addAll(methodNamesOnly);
        }
    }

    public void addMethods(ArrayList<MethodAndSignature> methods) {
        if (methods != null) {
            this.methods.addAll(methods);
        }
    }

    public boolean addMethod(String methodName) {
        methodNamesOnly.add(methodName);
        return true;
    }

    public boolean addMethod(String method, String signature) {
        if (method == null || method.length() == 0) {
            return false;
        }
        if (signature == null || signature.length() == 0) {
            return addMethod(method);
        } else {
            methods.add(new MethodAndSignature(method, signature));
            return true;
        }
    }
}
