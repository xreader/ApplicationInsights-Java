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

package com.microsoft.applicationinsights.agent.internal.agent.instrumentor;

import com.microsoft.applicationinsights.agent.internal.coresync.impl.ImplementationsCoordinator;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Created by gupele on 7/21/2015.
 */
public final class HttpClientMethodInstrumentor extends DefaultMethodInstrumentor {
    private final static String ON_ENTER_METHOD_NANE = "onMethodSendingURLEnter";
    private final static String ON_ENTER_METHOD_SIGNATURE = "(Ljava/lang/String;Ljava/lang/String;)V";

    private final String implementationCoordinatorInternalName;
    private final String implementationCoordinatorJavaName;

    public HttpClientMethodInstrumentor(int access, String desc, String owner, String methodName, MethodVisitor methodVisitor) {
        super(false, true, access, desc, owner, methodName, methodVisitor);
        implementationCoordinatorInternalName = Type.getInternalName(ImplementationsCoordinator.class);
        implementationCoordinatorJavaName = "L" + implementationCoordinatorInternalName + ";";
    }

    @Override
    public void onMethodEnter() {

        int basicRequestLocalIndex = this.newLocal(Type.getType(Object.class));
        int requestLineLocalIndex = this.newLocal(Type.getType(Object.class));

        mv.visitVarInsn(ALOAD, 2);
        Label nullLabel = new Label();
        mv.visitJumpInsn(IFNULL, nullLabel);

        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/http/client/methods/HttpRequestBase", "getRequestLine", "()Lorg/apache/http/RequestLine;", false);
        mv.visitVarInsn(ASTORE, basicRequestLocalIndex);

        mv.visitVarInsn(ALOAD, basicRequestLocalIndex);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/http/message/BasicRequestLine", "getUri", "()Ljava/lang/String;", false);
        mv.visitVarInsn(ASTORE, requestLineLocalIndex);

        super.visitFieldInsn(GETSTATIC, implementationCoordinatorInternalName, "INSTANCE", implementationCoordinatorJavaName);

        mv.visitLdcInsn(getMethodName());
        mv.visitVarInsn(ALOAD, requestLineLocalIndex);

        mv.visitMethodInsn(INVOKEVIRTUAL, implementationCoordinatorInternalName, ON_ENTER_METHOD_NANE, ON_ENTER_METHOD_SIGNATURE, false);

        Label notNullLabel = new Label();
        mv.visitJumpInsn(GOTO, notNullLabel);

        mv.visitLabel(nullLabel);
        super.visitFieldInsn(GETSTATIC, implementationCoordinatorInternalName, "INSTANCE", implementationCoordinatorJavaName);
        mv.visitLdcInsn(getMethodName());
        mv.visitInsn(ACONST_NULL);

        mv.visitMethodInsn(INVOKEVIRTUAL, implementationCoordinatorInternalName, ON_ENTER_METHOD_NANE, ON_ENTER_METHOD_SIGNATURE, false);

        mv.visitLabel(notNullLabel);
    }
}
