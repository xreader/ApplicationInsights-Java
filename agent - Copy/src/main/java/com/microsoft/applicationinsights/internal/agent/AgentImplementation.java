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

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.jar.JarFile;

/**
 * Created by gupele on 5/6/2015.
 */
public final class AgentImplementation {

    private final static String AGENT_JAR_NAME = "agent-1.0.jar";

    private static String agentJarLocation;

    public static void premain(String args, Instrumentation inst) {

        agentJarLocation = getAgentJarLocation();

        try {
            appendJarsToBootstrapClassLoader(inst);
            loadJarsToBootstrapClassLoader(inst);
        } catch (Throwable throwable) {
            System.out.println("Agent is NOT activated: failed to load to bootstrap class loader: " + throwable.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadJarsToBootstrapClassLoader(Instrumentation inst) throws Throwable {
        ClassLoader bcl = AgentImplementation.class.getClassLoader().getParent();
        Class<CodeInjector> cic = (Class<CodeInjector>) bcl.loadClass("com.microsoft.applicationinsights.internal.agent.CodeInjector");
        if (cic == null) {
            throw new IllegalStateException("Failed to load CodeInjector");
        }

        cic.getDeclaredConstructor(Instrumentation.class, String.class).newInstance(inst, agentJarLocation);
    }

    private static void appendJarsToBootstrapClassLoader(Instrumentation inst) throws Throwable {
        String agentJarPath = agentJarLocation.startsWith("file:/") ? agentJarLocation : "file:/" + agentJarLocation;
        URL configurationURL = new URL(agentJarPath + AGENT_JAR_NAME);

        JarFile agentJar = new JarFile(URLDecoder.decode(configurationURL.getFile(), "UTF-8"));

        inst.appendToBootstrapClassLoaderSearch(agentJar);

        // Guy: temporary until we use 'fat jar'
        URL url = getConfigurationFile("asm-5.0.3.jar");
        JarFile jarFile = new JarFile(URLDecoder.decode(url.getPath(), "UTF-8"));
        inst.appendToBootstrapClassLoaderSearch(jarFile);

        url = getConfigurationFile("asm-commons-5.0.3.jar");
        jarFile = new JarFile(URLDecoder.decode(url.getPath(), "UTF-8"));
        inst.appendToBootstrapClassLoaderSearch(jarFile);

        System.out.println("Successfully loaded Agent jar");
    }

    public static URL getConfigurationFile(String jarName) {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if ((systemClassLoader instanceof URLClassLoader)) {
                for (URL url : ((URLClassLoader)systemClassLoader).getURLs()) {
                    String urlPath = url.getPath();
                    System.out.println(url);
                    System.out.println(urlPath);
                    if (urlPath.endsWith(jarName)) {
                        System.out.println("Found jar" + jarName + " " + url.toString());
                        return url;
                    }
                }
            }
        } catch (Throwable throwable) {
            System.out.println("e:" + throwable.getMessage());
        }

        return AgentImplementation.class.getProtectionDomain().getCodeSource().getLocation();
    }

    public static String getAgentJarLocation() {
        try {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            if ((systemClassLoader instanceof URLClassLoader)) {
                for (URL url : ((URLClassLoader)systemClassLoader).getURLs()) {
                    String urlPath = url.getPath();
                    if (urlPath.endsWith(AGENT_JAR_NAME)) {
                        int index = urlPath.lastIndexOf('/');
                        urlPath = urlPath.substring(0, index + 1);
                        return urlPath;
                    }
                }
            }
        } catch (Throwable throwable) {
            System.out.println("e:" + throwable.getMessage());
        }

        return AgentImplementation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
