package com.microsoft.applicationinsights.internal.agent;

import com.microsoft.applicationinsights.internal.coresync.InstrumentedClassType;
import org.objectweb.asm.MethodVisitor;

/**
 * Created by gupele on 5/20/2015.
 */
public class MethodWrapperFactory {
    public EnterExitMethodWrapper getWrapper(InstrumentedClassType type, int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        if ("sun/net/www/protocol/http/HttpURLConnection".equals(owner) || "sun/net/www/protocol/http/HttpsURLConnection".equals(owner)) {
            return new EnterExitMethodForHttp(type, access, desc, owner, methodName, methodVisitor);
        } else {
            if ("com/mysql/jdbc/StatementImpl".endsWith(owner) ||
                "com/mysql/jdbc/PreparedStatement".endsWith(owner) ||
                "com/mysql/jdbc/ServerPreparedStatement".equals(owner)) {
                return new EnterExitMethodForSqlStatement(type, access, desc, owner, methodName, methodVisitor);
            }
        }

        return new EnterExitMethodWrapper(type, access, desc, owner, methodName, methodVisitor);
    }
}
