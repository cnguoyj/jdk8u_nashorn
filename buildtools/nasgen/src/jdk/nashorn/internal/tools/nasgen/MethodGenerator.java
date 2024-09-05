/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.nashorn.internal.tools.nasgen;

import static jdk.nashorn.internal.asm.Opcodes.AALOAD;
import static jdk.nashorn.internal.asm.Opcodes.AASTORE;
import static jdk.nashorn.internal.asm.Opcodes.ACC_STATIC;
import static jdk.nashorn.internal.asm.Opcodes.ACONST_NULL;
import static jdk.nashorn.internal.asm.Opcodes.ALOAD;
import static jdk.nashorn.internal.asm.Opcodes.ANEWARRAY;
import static jdk.nashorn.internal.asm.Opcodes.ARETURN;
import static jdk.nashorn.internal.asm.Opcodes.ASM4;
import static jdk.nashorn.internal.asm.Opcodes.ASTORE;
import static jdk.nashorn.internal.asm.Opcodes.BALOAD;
import static jdk.nashorn.internal.asm.Opcodes.BASTORE;
import static jdk.nashorn.internal.asm.Opcodes.BIPUSH;
import static jdk.nashorn.internal.asm.Opcodes.CALOAD;
import static jdk.nashorn.internal.asm.Opcodes.CASTORE;
import static jdk.nashorn.internal.asm.Opcodes.CHECKCAST;
import static jdk.nashorn.internal.asm.Opcodes.DALOAD;
import static jdk.nashorn.internal.asm.Opcodes.DASTORE;
import static jdk.nashorn.internal.asm.Opcodes.DCONST_0;
import static jdk.nashorn.internal.asm.Opcodes.DRETURN;
import static jdk.nashorn.internal.asm.Opcodes.DUP;
import static jdk.nashorn.internal.asm.Opcodes.DUP2;
import static jdk.nashorn.internal.asm.Opcodes.FALOAD;
import static jdk.nashorn.internal.asm.Opcodes.FASTORE;
import static jdk.nashorn.internal.asm.Opcodes.FCONST_0;
import static jdk.nashorn.internal.asm.Opcodes.FRETURN;
import static jdk.nashorn.internal.asm.Opcodes.GETFIELD;
import static jdk.nashorn.internal.asm.Opcodes.GETSTATIC;
import static jdk.nashorn.internal.asm.Opcodes.H_INVOKESTATIC;
import static jdk.nashorn.internal.asm.Opcodes.IALOAD;
import static jdk.nashorn.internal.asm.Opcodes.IASTORE;
import static jdk.nashorn.internal.asm.Opcodes.ICONST_0;
import static jdk.nashorn.internal.asm.Opcodes.ICONST_1;
import static jdk.nashorn.internal.asm.Opcodes.ILOAD;
import static jdk.nashorn.internal.asm.Opcodes.INVOKEINTERFACE;
import static jdk.nashorn.internal.asm.Opcodes.INVOKESPECIAL;
import static jdk.nashorn.internal.asm.Opcodes.INVOKESTATIC;
import static jdk.nashorn.internal.asm.Opcodes.INVOKEVIRTUAL;
import static jdk.nashorn.internal.asm.Opcodes.IRETURN;
import static jdk.nashorn.internal.asm.Opcodes.ISTORE;
import static jdk.nashorn.internal.asm.Opcodes.LALOAD;
import static jdk.nashorn.internal.asm.Opcodes.LASTORE;
import static jdk.nashorn.internal.asm.Opcodes.LCONST_0;
import static jdk.nashorn.internal.asm.Opcodes.LRETURN;
import static jdk.nashorn.internal.asm.Opcodes.NEW;
import static jdk.nashorn.internal.asm.Opcodes.POP;
import static jdk.nashorn.internal.asm.Opcodes.PUTFIELD;
import static jdk.nashorn.internal.asm.Opcodes.PUTSTATIC;
import static jdk.nashorn.internal.asm.Opcodes.RETURN;
import static jdk.nashorn.internal.asm.Opcodes.SALOAD;
import static jdk.nashorn.internal.asm.Opcodes.SASTORE;
import static jdk.nashorn.internal.asm.Opcodes.SIPUSH;
import static jdk.nashorn.internal.asm.Opcodes.SWAP;
import static jdk.nashorn.internal.tools.nasgen.StringConstants.INIT;
import static jdk.nashorn.internal.tools.nasgen.StringConstants.SPECIALIZATION_INIT2;
import static jdk.nashorn.internal.tools.nasgen.StringConstants.SPECIALIZATION_INIT3;
import static jdk.nashorn.internal.tools.nasgen.StringConstants.SPECIALIZATION_TYPE;
import static jdk.nashorn.internal.tools.nasgen.StringConstants.TYPE_SPECIALIZATION;
import java.util.List;
import jdk.nashorn.internal.asm.Handle;
import jdk.nashorn.internal.asm.MethodVisitor;
import jdk.nashorn.internal.asm.Type;
import jdk.nashorn.internal.objects.annotations.SpecializedFunction.LinkLogic;

/**
 * Base class for all method generating classes.
 *
 */
public class MethodGenerator extends MethodVisitor {
    private final int access;
    private final String name;
    private final String descriptor;
    private final Type returnType;
    private final Type[] argumentTypes;

    static final Type EMPTY_LINK_LOGIC_TYPE = Type.getType(LinkLogic.getEmptyLinkLogicClass());

    MethodGenerator(final MethodVisitor mv, final int access, final String name, final String descriptor) {
        super(ASM4, mv);
        this.access        = access;
        this.name          = name;
        this.descriptor    = descriptor;
        this.returnType    = Type.getReturnType(descriptor);
        this.argumentTypes = Type.getArgumentTypes(descriptor);
    }

    int getAccess() {
        return access;
    }

    final String getName() {
        return name;
    }

    final String getDescriptor() {
        return descriptor;
    }

    final Type getReturnType() {
        return returnType;
    }

    final Type[] getArgumentTypes() {
        return argumentTypes;
    }

    /**
     * Check whether access for this method is static
     * @return true if static
     */
    protected final boolean isStatic() {
        return (getAccess() & ACC_STATIC) != 0;
    }

    /**
     * Check whether this method is a constructor
     * @return true if constructor
     */
    protected final boolean isConstructor() {
        return "<init>".equals(name);
    }

    void newObject(final String type) {
        super.visitTypeInsn(NEW, type);
    }

    void newObjectArray(final String type) {
        super.visitTypeInsn(ANEWARRAY, type);
    }

    void loadThis() {
        if ((access & ACC_STATIC) != 0) {
            throw new IllegalStateException("no 'this' inside static method");
        }
        super.visitVarInsn(ALOAD, 0);
    }

    void returnValue() {
        super.visitInsn(returnType.getOpcode(IRETURN));
    }

    void returnVoid() {
        super.visitInsn(RETURN);
    }

    // load, store
    void arrayLoad(final Type type) {
        super.visitInsn(type.getOpcode(IALOAD));
    }

    void arrayLoad() {
        super.visitInsn(AALOAD);
    }

    void arrayStore(final Type type) {
        super.visitInsn(type.getOpcode(IASTORE));
    }

    void arrayStore() {
        super.visitInsn(AASTORE);
    }

    void loadLiteral(final Object value) {
        super.visitLdcInsn(value);
    }

    void classLiteral(final String className) {
        super.visitLdcInsn(className);
    }

    void loadLocal(final Type type, final int index) {
        super.visitVarInsn(type.getOpcode(ILOAD), index);
    }

    void loadLocal(final int index) {
        super.visitVarInsn(ALOAD, index);
    }

    void storeLocal(final Type type, final int index) {
        super.visitVarInsn(type.getOpcode(ISTORE), index);
    }

    void storeLocal(final int index) {
        super.visitVarInsn(ASTORE, index);
    }

    void checkcast(final String type) {
        super.visitTypeInsn(CHECKCAST, type);
    }

    // push constants/literals
    void pushNull() {
        super.visitInsn(ACONST_NULL);
    }

    void push(final int value) {
        if (value >= -1 && value <= 5) {
            super.visitInsn(ICONST_0 + value);
        } else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            super.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            super.visitIntInsn(SIPUSH, value);
        } else {
            super.visitLdcInsn(value);
        }
    }

    void loadClass(final String className) {
        super.visitLdcInsn(Type.getObjectType(className));
    }

    void pop() {
        super.visitInsn(POP);
    }

    // various "dups"
    void dup() {
        super.visitInsn(DUP);
    }

    void dup2() {
        super.visitInsn(DUP2);
    }

    void swap() {
        super.visitInsn(SWAP);
    }

    void dupArrayValue(final int arrayOpcode) {
        switch (arrayOpcode) {
            case IALOAD: case FALOAD:
            case AALOAD: case BALOAD:
            case CALOAD: case SALOAD:
            case IASTORE: case FASTORE:
            case AASTORE: case BASTORE:
            case CASTORE: case SASTORE:
                dup();
            break;

            case LALOAD: case DALOAD:
            case LASTORE: case DASTORE:
                dup2();
            break;
            default:
                throw new AssertionError("invalid dup");
        }
    }

    void dupReturnValue(final int returnOpcode) {
        switch (returnOpcode) {
            case IRETURN:
            case FRETURN:
            case ARETURN:
                super.visitInsn(DUP);
                return;
            case LRETURN:
            case DRETURN:
                super.visitInsn(DUP2);
                return;
            case RETURN:
                return;
            default:
                throw new IllegalArgumentException("not return");
        }
    }

    void dupValue(final Type type) {
        switch (type.getSize()) {
            case 1:
                dup();
            break;
            case 2:
                dup2();
            break;
            default:
                throw new AssertionError("invalid dup");
        }
    }

    void dupValue(final String desc) {
        final int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                super.visitInsn(DUP);
                break;
            case 'J':
            case 'D':
                super.visitInsn(DUP2);
                break;
            default:
                throw new RuntimeException("invalid signature");
        }
    }

    // push default value of given type desc
    void defaultValue(final String desc) {
        final int typeCode = desc.charAt(0);
        switch (typeCode) {
            case '[':
            case 'L':
                super.visitInsn(ACONST_NULL);
                break;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                super.visitInsn(ICONST_0);
                break;
            case 'J':
                super.visitInsn(LCONST_0);
                break;
            case 'F':
                super.visitInsn(FCONST_0);
                break;
            case 'D':
                super.visitInsn(DCONST_0);
                break;
            default:
                throw new AssertionError("invalid desc " + desc);
        }
    }

    // invokes, field get/sets
    void invokeInterface(final String owner, final String method, final String desc) {
        super.visitMethodInsn(INVOKEINTERFACE, owner, method, desc, true);
    }

    void invokeVirtual(final String owner, final String method, final String desc) {
        super.visitMethodInsn(INVOKEVIRTUAL, owner, method, desc, false);
    }

    void invokeSpecial(final String owner, final String method, final String desc) {
        super.visitMethodInsn(INVOKESPECIAL, owner, method, desc, false);
    }

    void invokeStatic(final String owner, final String method, final String desc) {
        super.visitMethodInsn(INVOKESTATIC, owner, method, desc, false);
    }

    void putStatic(final String owner, final String field, final String desc) {
        super.visitFieldInsn(PUTSTATIC, owner, field, desc);
    }

    void getStatic(final String owner, final String field, final String desc) {
        super.visitFieldInsn(GETSTATIC, owner, field, desc);
    }

    void putField(final String owner, final String field, final String desc) {
        super.visitFieldInsn(PUTFIELD, owner, field, desc);
    }

    void getField(final String owner, final String field, final String desc) {
        super.visitFieldInsn(GETFIELD, owner, field, desc);
    }

    private static boolean linkLogicIsEmpty(final Type type) {
        assert EMPTY_LINK_LOGIC_TYPE != null; //type is ok for null if we are a @SpecializedFunction without any attribs
        return EMPTY_LINK_LOGIC_TYPE.equals(type);
    }

    void memberInfoArray(final String className, final List<MemberInfo> mis) {
        if (mis.isEmpty()) {
            pushNull();
            return;
        }

        int pos = 0;
        push(mis.size());
        newObjectArray(SPECIALIZATION_TYPE);
        for (final MemberInfo mi : mis) {
            dup();
            push(pos++);
            visitTypeInsn(NEW, SPECIALIZATION_TYPE);
            dup();
            visitLdcInsn(new Handle(H_INVOKESTATIC, className, mi.getJavaName(), mi.getJavaDesc()));
            final Type    linkLogicClass = mi.getLinkLogicClass();
            final boolean linkLogic      = !linkLogicIsEmpty(linkLogicClass);
            final String  ctor           = linkLogic ? SPECIALIZATION_INIT3 : SPECIALIZATION_INIT2;
            if (linkLogic) {
                visitLdcInsn(linkLogicClass);
            }
            visitInsn(mi.isOptimistic() ? ICONST_1 : ICONST_0);
            visitMethodInsn(INVOKESPECIAL, SPECIALIZATION_TYPE, INIT, ctor, false);
            arrayStore(TYPE_SPECIALIZATION);
        }
    }

    void computeMaxs() {
        // These values are ignored as we create class writer
        // with ClassWriter.COMPUTE_MAXS flag.
        super.visitMaxs(Short.MAX_VALUE, Short.MAX_VALUE);
    }

    // debugging support - print calls
    void println(final String msg) {
        super.visitFieldInsn(GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;");
        super.visitLdcInsn(msg);
        super.visitMethodInsn(INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/String;)V",
                    false);
    }

    // print the object on the top of the stack
    void printObject() {
        super.visitFieldInsn(GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;");
        super.visitInsn(SWAP);
        super.visitMethodInsn(INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(Ljava/lang/Object;)V",
                    false);
    }
}
