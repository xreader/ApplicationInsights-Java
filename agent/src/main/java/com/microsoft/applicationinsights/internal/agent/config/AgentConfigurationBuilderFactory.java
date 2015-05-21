package com.microsoft.applicationinsights.internal.agent.config;

/**
 * Created by gupele on 5/19/2015.
 */
public class AgentConfigurationBuilderFactory {
    public AgentConfigurationBuilder createBuilder(String builderClassName) {
        if (builderClassName == null || builderClassName.length() == 0) {
            return createDefaultBuilder();
        }
        try {
            Object builder = Class.forName(builderClassName).newInstance();
            if (builder instanceof AgentConfigurationBuilder) {
                return (AgentConfigurationBuilder)builder;
            }
        } catch (Throwable t) {
            System.out.println("Failed to create builder: '%s'" + t.getMessage());
        }

        return null;
    }

    public AgentConfigurationBuilder createDefaultBuilder() {
        return new XmlAgentConfigurationBuilder();
    }
}
