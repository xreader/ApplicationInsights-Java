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

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import com.microsoft.applicationinsights.internal.agent.config.AgentConfiguration;
import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;

/**
 * Created by gupele on 5/11/2015.
 */
class DefaultClassNamesProvider implements ClassNamesProvider {
    private AgentConfiguration agentConfiguration;

    private final HashSet<String> forbiddenPaths = new HashSet<String>();

    public DefaultClassNamesProvider() {
        forbiddenPaths.add("java/");
        forbiddenPaths.add("javax/");
        forbiddenPaths.add("org/apache");
        forbiddenPaths.add("com/microsoft/applicationinsights");
        forbiddenPaths.add("sun/nio/");
        forbiddenPaths.add("sun/rmi/");
        forbiddenPaths.add("com/sun/jmx/");
        forbiddenPaths.add("sun/net/www/http/KeepAlive");
    }

    @Override
    public Map<String, ClassInstrumentationData> getClassesAndMethods() {
        boolean builtInDisabled = false;
        HashMap<String, ClassInstrumentationData> classes = new HashMap<String, ClassInstrumentationData>();

        if (agentConfiguration != null) {
            builtInDisabled = agentConfiguration.isBuiltInDisabled();
            HashMap<String, ClassInstrumentationData> configurationData = agentConfiguration.getRequestedClassesToInstrument();
            if (configurationData != null) {
                classes.putAll(configurationData);
            }
        }

        if (!builtInDisabled) {
            addHibernate(classes);
            addUrlConnection(classes);
            addMySql(classes);
        }

        return classes;
    }

    @Override
    public Map<String, ClassInstrumentationData> getInterfaces() {
        HashMap<String, ClassInstrumentationData> interfaces = new HashMap<String, ClassInstrumentationData>();
//        HashSet<String> methodNamesOnly = new HashSet<String>();
//        methodNamesOnly.add("execute");
//        methodNamesOnly.add("executeQuery");
//        interfaces.put("java/sql/Statement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));

//        methodNamesOnly.clear();
//        methodNamesOnly.add("run");
//        interfaces.put("java/lang/Runnable", new ClassInstrumentationData(InstrumentedClassType.UNDEFINED, null, methodNamesOnly));
//
//        methodNamesOnly.clear();
//        methodNamesOnly.add("call");
//        interfaces.put("java/util/concurrent/Callable", new ClassInstrumentationData(InstrumentedClassType.UNDEFINED, null, methodNamesOnly));
//
        return interfaces;
    }

    @Override
    public void setConfiguration(AgentConfiguration agentConfiguration) {
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public HashSet<String> getForbiddenPaths() {
        return forbiddenPaths;
    }

    private void addMySql(HashMap<String, ClassInstrumentationData> classes) {
        HashSet<String> methodNamesOnly = new HashSet<String>();
        methodNamesOnly.add("execute");
        methodNamesOnly.add("executeQuery");
        classes.put("com/mysql/jdbc/StatementImpl", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/PreparedStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/ServerPreparedStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/CallableStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/JDBC4CallableStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/JDBC4PreparedStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/JDBC4ServerPreparedStatement", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/jdbc2/optional/StatementWrapper", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/jdbc2/optional/JDBC4StatementWrapper", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/jdbc2/optional/CallableStatementWrapper", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
        classes.put("com/mysql/jdbc/jdbc2/optional/JDBC4PreparedStatementWrapper", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));
    }

    private void addHibernate(HashMap<String, ClassInstrumentationData> classes) {
        HashSet<String> methodNamesOnly = new HashSet<String>();
        methodNamesOnly.add("delete");
        methodNamesOnly.add("execute");
        methodNamesOnly.add("executeNativeUpdate");
        methodNamesOnly.add("executeUpdate");
        methodNamesOnly.add("find");
        methodNamesOnly.add("get");
        methodNamesOnly.add("save");
        methodNamesOnly.add("list");
        methodNamesOnly.add("load");
        methodNamesOnly.add("saveOrUpdate");
        methodNamesOnly.add("update");

        classes.put("org/hibernate/impl/SessionImpl", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));

        methodNamesOnly.clear();
        methodNamesOnly.add("delete");
        methodNamesOnly.add("get");
        methodNamesOnly.add("insert");
        methodNamesOnly.add("list");
        methodNamesOnly.add("update");

        classes.put("org/hibernate/impl/StatelessSessionImpl", new ClassInstrumentationData(InstrumentedClassType.SQL, null, methodNamesOnly));

        ArrayList<MethodAndSignature> methodAndSignatures = new ArrayList<MethodAndSignature>();
        methodAndSignatures.add(new MethodAndSignature("getInputStream", "()Ljava/io/InputStream;"));
        classes.put("sun/net/www/protocol/http/HttpURLConnection", new ClassInstrumentationData(InstrumentedClassType.SQL, methodAndSignatures, null));
        classes.put("sun/net/www/protocol/http/HttpsURLConnection", new ClassInstrumentationData(InstrumentedClassType.SQL, methodAndSignatures, null));
    }

    private void addUrlConnection(HashMap<String, ClassInstrumentationData> classes) {
//        ArrayList<MethodAndSignature> methods = new ArrayList<MethodAndSignature>();
//
//        methods.add(new MethodAndSignature("read", "([BII)I"));
//
//        classes.put("sun/net/www/protocol/http/HttpURLConnection$HttpInputStream", new ClassInstrumentationData(InstrumentedClassType.HTTP, methods, null));
    }
}
