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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gupele on 5/11/2015.
 */
final class SuperVerifier extends ClassVisitor {
    private final Set<String> interfaceNames;
    private final HashSet<String> found = new HashSet<String>();

    public SuperVerifier(Set<String> interfaceNames, ClassWriter cv) {
        super(Opcodes.ASM5, cv);
        this.interfaceNames = interfaceNames;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        for (String i : interfaces) {
            if (interfaceNames.contains(i)) {
                found.add(i);
            }
        }

        if (found.isEmpty()) {
            throw new IllegalArgumentException();
        }

        cv.visit(version, access, name, signature, superName, interfaces);
    }

    public Set<String> getFound() {
        return found;
    }
}
