package com.microsoft.applicationinsights.internal.agent.config;

import com.microsoft.applicationinsights.internal.agent.ClassInstrumentationData;

import java.util.HashMap;

/**
 * Created by gupele on 5/19/2015.
 */
class XmlAgentConfiguration implements AgentConfiguration {
    private HashMap<String, ClassInstrumentationData> classesToInstrument;
    private boolean builtInDisabled;

    public static AgentConfiguration build(String baseFolder, AgentConfigurationBuilder suggestedBuilder) {
        AgentConfigurationBuilder builder = suggestedBuilder != null ? suggestedBuilder : new XmlAgentConfigurationBuilder();
        return builder.parseConfigurationFile(baseFolder);
    }

    void setRequestedClassesToInstrument(HashMap<String, ClassInstrumentationData> classesToInstrument) {
        System.out.println("size : " + classesToInstrument);
        if (classesToInstrument != null) {
            System.out.println("sizein:" + classesToInstrument.size());
        }
        this.classesToInstrument = classesToInstrument;
    }

    @Override
    public HashMap<String, ClassInstrumentationData> getRequestedClassesToInstrument() {
        System.out.println("sizea: " + classesToInstrument);
        if (classesToInstrument != null) {
            System.out.println("sizeon:" + classesToInstrument.size());
        }
        return classesToInstrument;
    }

    @Override
    public boolean isBuiltInDisabled() {
        return builtInDisabled;
    }

    public void setBuiltInDisabled(boolean builtInDisabled) {
        this.builtInDisabled = builtInDisabled;
    }
}
