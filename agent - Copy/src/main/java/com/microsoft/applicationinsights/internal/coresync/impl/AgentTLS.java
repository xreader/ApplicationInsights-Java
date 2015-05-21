package com.microsoft.applicationinsights.internal.coresync.impl;

/**
 * The class is used to set and get a name on application thread.
 * The name can be used to fetch information on the part of the application, typically a WebApp, that owns that thread
 *
 * Created by gupele on 5/6/2015.
 */
public final class AgentTLS  {

    private static final InheritableThreadLocal<String> tlsData = new InheritableThreadLocal<String>();

    public static String getTLSKey() {
        return tlsData.get();
    }

    public static void setTLSKey(String value) {
        tlsData.set(value);
    }
}
