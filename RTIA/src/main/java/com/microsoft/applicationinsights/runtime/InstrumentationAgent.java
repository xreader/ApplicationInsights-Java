package com.microsoft.applicationinsights.runtime;

import java.lang.instrument.Instrumentation;

/**
 * Methods that would have been great to have on maps.
 */
public class InstrumentationAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Executing premain.........");
        inst.addTransformer(new DurationTransformer());
        System.out.println("Executing premain finished.........");
    }
}
