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

import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.microsoft.applicationinsights.internal.coresync.AgentNotificationsHandler;
import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;

/**
 * The Core's implementation: we will start measuring time on enter and stop on finish.
 * We will then send a Telemetry that captures the method name, time and the result of the method.
 *
 * Created by gupele on 5/7/2015.
 */
final class CoreAgentNotificationsHandler implements AgentNotificationsHandler {

    private static class MethodData {
        public String name;
        public String[] arguments;
        public long interval;
        public InstrumentedClassType type;
        public Object result;
    }

    private static class ThreadData {
        public final LinkedList<MethodData> methods = new LinkedList<MethodData>();

        public void done() {
            methods.clear();
        }
    }

    static final class ThreadDataThreadLocal extends ThreadLocal<ThreadData> {
        private ThreadData threadData;

        @Override
        protected ThreadData initialValue() {
            threadData = new ThreadData();
            return threadData;
        }
    };

    private ThreadDataThreadLocal threadDataThreadLocal = new ThreadDataThreadLocal();

    private final String name;

    public CoreAgentNotificationsHandler(String name) {
        this.name = name;
    }

    @Override
    public void onMethodEnterURL(String name, URL url) {
        System.out.println("core _:" + name + " " + url);
        String urlAsString = url == null ? null : url.toString();
        startMethod(InstrumentedClassType.UNDEFINED, name, urlAsString);
    }

    @Override
    public void onMethodEnterSqlStatement(String name, Statement statement, String sqlStatement) {
        if (statement == null) {
            return;
        }

        try {
            Connection connection = statement.getConnection();
            if (connection == null) {
                return;
            }

            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData == null) {
                return;
            }

            String url = metaData.getURL();
            startMethod(InstrumentedClassType.UNDEFINED, name, url, sqlStatement);

        } catch (SQLException e) {
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onDefaultMethodEnter(String name) {
        startMethod(InstrumentedClassType.UNDEFINED, name, new String[]{});
    }

    @Override
    public void onMethodFinish(String name, Throwable throwable) {
    }

    @Override
    public void onMethodFinish(String name) {
        finalizeMethod(null, false);
    }

    @Override
    public void onMethodFinish(String name, int result) {
        finalizeMethod(Integer.valueOf(result), false);
    }

    @Override
    public void onMethodFinish(String name, long result) {
        finalizeMethod(Long.valueOf(result), false);
    }

    @Override
    public void onMethodFinish(String name, double result) {
        finalizeMethod(Double.valueOf(result), false);
    }

    @Override
    public void onMethodFinish(String name, float result) {
        finalizeMethod(Float.valueOf(result), false);
    }

    @Override
    public void onMethodFinish(String name, Object result) {
        finalizeMethod(result, false);
    }

    @Override
    public void onMethodFinish(Object exception, String name) {
        finalizeMethod(exception, true);
    }

    @Override
    public void onNewThread(String key) {
    }

    private void startMethod(InstrumentedClassType type, String name, String... arguments) {
        long start = System.nanoTime();

        ThreadData localData = threadDataThreadLocal.get();
        MethodData methodData = new MethodData();
        methodData.interval = start;
        methodData.type = type;
        methodData.arguments = arguments;
        methodData.name = name;
        localData.methods.addFirst(methodData);
    }

    private void finalizeMethod(Object result, boolean exception) {
        long finish = System.nanoTime();

        ThreadData localData = threadDataThreadLocal.get();
        if (localData.methods == null || localData.methods.isEmpty()) {
            return;
        }

        MethodData methodData = localData.methods.removeFirst();
        if (methodData == null) {
            return;
        }

        methodData.interval = finish - methodData.interval;
        methodData.result = result;

        report(methodData, exception);
    }

    private void report(MethodData methodData, boolean isException) {
        System.out.println("report : " + methodData.name + " " + methodData.type + " " + methodData.interval / 1000000000.0 + " seconds" + (isException ? " with exception" : " with no exception"));
        if (methodData.arguments != null) {
            for (String arg : methodData.arguments) {
                System.out.println(arg);
            }
        }
        System.out.println("-------");
    }
}
