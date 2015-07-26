/*
 * AppInsights-Java
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

package com.microsoft.applicationinsights.agent.internal.agent;

/**
 * Created by gupele on 7/26/2015.
 */
public class SqlBuiltInClassesToInstrument {

    private final static String[] JDBC_CLASS_NAMES = new String[] {
            "com/mysql/jdbc/StatementImpl",
            "com/mysql/jdbc/PreparedStatement",
            "com/mysql/jdbc/ServerPreparedStatement",
            "com/mysql/jdbc/CallableStatement",
            "com/mysql/jdbc/JDBC4CallableStatement",
            "com/mysql/jdbc/JDBC4PreparedStatement",
            "com/mysql/jdbc/JDBC4ServerPreparedStatement",
            "com/mysql/jdbc/jdbc2/optional/StatementWrapper",
            "com/mysql/jdbc/jdbc2/optional/JDBC4StatementWrapper",
            "com/mysql/jdbc/jdbc2/optional/CallableStatementWrapper",
            "com/mysql/jdbc/jdbc2/optional/JDBC4PreparedStatementWrapper",

            "org/sqlite/jdbc4/JDBC4Statement",
            "org/sqlite/core/CorePreparedStatement",
            "org/sqlite/jdbc3/JDBC3PreparedStatement",
            "org/sqlite/jdbc4/JDBC4PreparedStatement",

            "org/hsqldb/jdbc/JDBCPreparedStatement",
            "org/hsqldb/jdbc/jdbcCallableStatement",
            "org/hsqldb/jdbc/JDBCStatement",

            "org/postgresql/core/BaseStatement",
            "org/postgresql/jdbc2/AbstractJdbc2Statement",
            "org/postgresql/jdbc3g/AbstractJdbc3gStatement",
            "org/postgresql/jdbc4/AbstractJdbc4Statement",
            "org/postgresql/jdbc4/Jdbc4Statement",
            "org/postgresql/jdbc4/Jdbc4PreparedStatement",
            "org/postgresql/jdbc4/Jdbc4CallableStatement"
    };

    private final static String[] JDBC_METHODS_TO_TRACK = {
            "execute",
            "executeQuery",
            "executeUpdate"
    };
}
