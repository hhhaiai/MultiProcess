package com.analysys.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;


public class CostClassVisitor extends ClassVisitor {

    boolean enable, costAll;
    String classname;

    public CostClassVisitor(ClassVisitor classVisitor, boolean enable, boolean costAll) {
        super(Opcodes.ASM5, classVisitor);
        this.enable = enable;
        this.costAll = costAll;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        classname = name.replace("/", ".");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        mv = new AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {

            private boolean inject = false;

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (Type.getDescriptor(Cost.class).equals(desc)) {
                    inject = true;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            protected void onMethodEnter() {
                if (isInject()) {
                    mv.visitLdcInsn(getName(name));
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/analysys/plugin/TimePrint", "start", "(Ljava/lang/String;)V", false);
                }
            }

            private String getName(String name) {
                Type[] types = Type.getArgumentTypes(desc);
                Type returnType = Type.getReturnType(desc);
                String type = "";
                for (int i = 0; i < types.length; i++) {
                    type = type.concat(types[i].getClassName());
                    if (i != types.length - 1) {
                        type = type.concat(",");
                    }
                }
                name = classname.concat(".").concat(name).concat("(").concat(type).concat(") ↑ ").concat(returnType.getClassName());
                return name;
            }

            private boolean isInject() {
                return enable && (costAll || inject);
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (isInject()) {
                    mv.visitLdcInsn(getName(name));
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/analysys/plugin/TimePrint", "end", "(Ljava/lang/String;)V", false);
                }
            }
        };
        return mv;
    }
}
