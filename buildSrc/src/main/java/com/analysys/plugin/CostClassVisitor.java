package com.analysys.plugin;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 方法耗时统计插桩器
 * @Version: 1.0
 * @Create: 2019-11-13 11:19:08
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class CostClassVisitor extends ClassVisitor {

    boolean enable, costAll;

    public CostClassVisitor(ClassVisitor classVisitor, boolean enable, boolean costAll) {
        super(Opcodes.ASM5, classVisitor);
        this.enable = enable;
        this.costAll = costAll;
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
                String type = "";
                for (int i = 0; i < types.length; i++) {
                    type = type.concat(types[i].getClassName());
                    if (i != types.length - 1) {
                        type = type.concat(",");
                    }
                }
                name = name.concat("[").concat(type).concat("]");
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
