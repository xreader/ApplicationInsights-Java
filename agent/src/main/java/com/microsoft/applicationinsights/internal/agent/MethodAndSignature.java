package com.microsoft.applicationinsights.internal.agent;

/**
 * Created by gupele on 5/19/2015.
 */
final class MethodAndSignature {
    public final String methodName;
    public final String methodSignature;

    MethodAndSignature(String methodName, String methodSignature) {
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }
}
