package com.microsoft.applicationinsights.internal.agent.config;

/**
 * Created by gupele on 5/19/2015.
 */
public interface AgentConfigurationBuilder {
    AgentConfiguration parseConfigurationFile(String baseFolder);
}
