package com.microsoft.applicationinsights.internal.coresync.impl;

import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Opcodes;

import com.microsoft.applicationinsights.internal.coresync.AgentNotificationsHandler;
import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;

/**
 * The class serves as the contact point between injected code and its real implementation.
 * Injected code will notify on various events in the class, calling this class, the class
 * will then delegate the call to the relevant 'client' by consulting the data on the Thread's TLS
 *
 * Note that the called methods should not throw any exception or error otherwise that might affect user's code
 *
 * Created by gupele on 5/6/2015.
 */
public enum ImplementationsCoordinator implements AgentNotificationsHandler {
    INSTANCE;

    @Override
    public void onSQLMethodEnter(String name, Object[] arrayOfValues) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onSQLMethodEnter(name, arrayOfValues);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onHTTPMethodEnter(String name, Object[] arrayOfValues) {
        try {
            for (Object value : arrayOfValues) {
                if (value instanceof URL) {
                    System.out.println("URL  : " + value);
                }
            }
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onHTTPMethodEnter(name, arrayOfValues);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onDefaultMethodEnter(String name, Object[] arrayOfValues) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onDefaultMethodEnter(name, arrayOfValues);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, Throwable throwable) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, throwable);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, int result) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, result);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, long result) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, result);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, double result) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, result);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, float result) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, result);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(String name, Object result) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(name, result);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public void onMethodFinish(Object exception, String name) {
        try {
            AgentNotificationsHandler implementation = getImplementation();
            if (implementation != null) {
                implementation.onMethodFinish(exception, name);
            }
        } catch (Throwable t) {
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void onNewThread(String key) {
        System.out.println(" tthread: " + Thread.currentThread().getId() + " " + key);
        AgentTLS.setTLSKey(key);
    }

    public static String onMethodEndName() {
        return "onMethodFinish";
    }

    public static String onMethodEndSignature(int opcode) {
        switch (opcode) {
            case Opcodes.IRETURN:
                return "(Ljava/lang/String;I)V";
            case Opcodes.FRETURN:
                return "(Ljava/lang/String;F)V";
            case Opcodes.LRETURN:
                return "(Ljava/lang/String;J)V";
            case Opcodes.DRETURN:
                return "(Ljava/lang/String;D)V";
            case Opcodes.ARETURN:
                return "(Ljava/lang/String;Ljava/lang/Object;)V";
            case Opcodes.ATHROW:
                return "(Ljava/lang/Object;Ljava/lang/String;)V";
            case Opcodes.RETURN:
                return "(Ljava/lang/String;)V";

            default:
                throw new RuntimeException("Unknown opcode");
        }
    }

    public static String onMethodEnterName(InstrumentedClassType type) {
        switch (type) {
            case SQL:
                return "onSQLMethodEnter";

            case HTTP:
                return "onHTTPMethodEnter";

            default:
                return "onHTTPMethodEnter";
        }
    }

    /**
     * The data we expect to have for every thread
     */
    public static class RegistrationData {
        public final ClassLoader classLoader;
        public final AgentNotificationsHandler handler;
        public final String key;

        public RegistrationData(ClassLoader classLoader, AgentNotificationsHandler handler, String key) {
            this.classLoader = classLoader;
            this.handler = handler;
            this.key = key;
        }
    }

    private static ConcurrentHashMap<String, RegistrationData> implementations = new ConcurrentHashMap<String, RegistrationData>();

    public String register(ClassLoader classLoader, AgentNotificationsHandler handler) {
        try {
            if (handler == null) {
                throw new IllegalArgumentException("AgentNotificationsHandler must be a non-null value");
            }

            String implementationName = handler.getName();
            if (implementationName == null || implementationName.length() == 0) {
                throw new IllegalArgumentException("AgentNotificationsHandler name must have be a non-null non empty value");
            }

            implementations.put(implementationName, new RegistrationData(classLoader, handler, implementationName));

            return implementationName;
        } catch (Throwable throwable) {
            System.out.println(String.format("Exception: '%s'", throwable.getMessage()));
            return null;
        }
    }

    public Collection<RegistrationData> getRegistered() {
        return implementations.values();
    }

    private AgentNotificationsHandler getImplementation() {
        String key = AgentTLS.getTLSKey();
        if (key != null && key.length() > 0) {
            RegistrationData implementation = implementations.get(key);
            if (implementation != null) {
                return implementation.handler;
            }
        }

        return null;
    }
}
