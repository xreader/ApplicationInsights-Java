package com.microsoft.applicationinsights.internal.agent.config;

import com.microsoft.applicationinsights.internal.agent.ClassInstrumentationData;

import java.util.HashMap;

/**
 * Created by gupele on 5/17/2015.
 */
public interface AgentConfiguration {

    boolean isBuiltInDisabled();

    HashMap<String, ClassInstrumentationData> getRequestedClassesToInstrument();
}
