package sf.codegen;


import org.springframework.asm.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import sf.accelerator.asm.commons.AnnotationDef;
import sf.accelerator.asm.commons.FieldExtCallback;
import sf.accelerator.asm.commons.FieldExtDef;
import sf.database.DBObject;
import sf.database.IDBEntity;
import sf.tools.ASMUtils;
import sf.tools.ArrayUtils;
import sf.tools.IOUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.SpringStringUtils;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EnhanceTaskASM {
    private ResourceLoader root;

    public EnhanceTaskASM(ResourceLoader root) {
        super();
        this.root = root;
    }

    public EnhanceTaskASM() {
    }

    /**
     * @param classdata
     * @param fieldEumData 允许传入null
     * @return 返回null表示不需要增强，返回byte[0]表示该类已经增强，返回其他数据为增强后的class
     * @throws Exception
     */
    public byte[] doEnhance(byte[] classdata, byte[] fieldEumData) throws Exception {
        Assert.notNull(classdata, "");
        List<String> enumFields = parseEnumFields(fieldEumData);
        try {
            ClassReader reader = new ClassReader(classdata);
            byte[] data = enhanceClass(reader, enumFields);
            // {
            // DEBUG
            // File file = new File("c:/asm/" +
            // StringUtils.substringAfterLast(className, ".") + ".class");
            // IOUtils.saveAsFile(file, data);
            // System.out.println(file +
            // " saved -- Enhanced class"+className);
            // }
            return data;
        } catch (EnhancedException e) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }

    }

    public List<String> parseEnumFields(byte[] fieldEumData) {
        final List<String> enumFields = new ArrayList<String>();
        if (fieldEumData != null) {
            ClassReader reader = new ClassReader(fieldEumData);
            reader.accept(new ClassVisitor(Opcodes.ASM5) {
                @Override
                public FieldVisitor visitField(int access, String name, String desc, String sig, Object value) {
                    if ((access & Opcodes.ACC_ENUM) > 0) {
                        enumFields.add(name);
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE);
        }
        return enumFields;
    }

    private static class EnhancedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public byte[] enhanceClass(ClassReader reader, final List<String> enumFields) {

        if ((reader.getAccess() & Opcodes.ACC_PUBLIC) == 0) {
            return null;// 非公有跳过
        }

        boolean isEntityInterface = isEntityClass(reader.getInterfaces(), reader.getSuperName(), !enumFields.isEmpty());
        if (!isEntityInterface)
            return null;

        ClassWriter cw = new ClassWriter(0);
        reader.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            private List<String> nonStaticFields = new ArrayList<String>();
            private List<String> lobAndRefFields = new ArrayList<String>();
            private String typeName;

            @Override
            public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
                this.typeName = name.replace('.', '/');
                if (version == Opcodes.V1_7) {

                }
                if ("sf/database/DBObject".equals(superName)) {

                } else {

                }
                super.visit(version, access, name, sig, superName, interfaces);
            }

            @Override
            public void visitOuterClass(String owner, String name, String desc) {
                super.visitOuterClass(owner, name, desc);
            }

            @Override
            public void visitAttribute(Attribute attr) {
                if ("sfd".equals(attr.type)) {
                    throw new EnhancedException();
                }
                super.visitAttribute(attr);
            }

            @Override
            public void visitEnd() {
                Attribute attr = new AttributeDef("sfd", new byte[]{0x1f});
                super.visitAttribute(attr);
            }

            @Override
            public FieldVisitor visitField(final int access, final String name, final String desc, String sig,
                                           final Object value) {
                FieldVisitor visitor = super.visitField(access, name, desc, sig, value);
                if ((access & Opcodes.ACC_STATIC) > 0)
                    return visitor;
                nonStaticFields.add(name);
                return new FieldExtDef(new FieldExtCallback(visitor) {
                    @Override
                    public void onFieldRead(FieldExtDef info) {
                        boolean contains = enumFields.contains(name);
                        if (contains) {
                            AnnotationDef annotation = info.getAnnotation("Ljavax/persistence/Lob;");
                            if (annotation != null) {
                                lobAndRefFields.add(name);
                            }
                        } else {
                            Object o = null;
                            if (o == null)
                                o = info.getAnnotation(OneToMany.class);
                            if (o == null)
                                o = info.getAnnotation(ManyToOne.class);
                            if (o == null)
                                o = info.getAnnotation(ManyToMany.class);
                            if (o == null)
                                o = info.getAnnotation(OneToOne.class);
                            if (o != null) {
                                lobAndRefFields.add(name);
                            }
                        }
                    }
                });
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
                String fieldName;
                if (name.startsWith("get")) {
                    fieldName = SpringStringUtils.uncapitalize(name.substring(3));
                    return asGetter(fieldName, access, name, desc, exceptions, sig);
                } else if (name.startsWith("is")) {
                    fieldName = SpringStringUtils.uncapitalize(name.substring(2));
                    return asGetter(fieldName, access, name, desc, exceptions, sig);
                } else if (name.startsWith("set")) {
                    fieldName = SpringStringUtils.uncapitalize(name.substring(3));
                    return asSetter(fieldName, access, name, desc, exceptions, sig);
                }
                return super.visitMethod(access, name, desc, sig, exceptions);
            }

            private MethodVisitor asGetter(String fieldName, int access, String name, String desc, String[] exceptions,
                                           String sig) {
                MethodVisitor mv = super.visitMethod(access, name, desc, sig, exceptions);
                Type[] types = Type.getArgumentTypes(desc);
                if (fieldName.length() == 0 || types.length > 0)
                    return mv;
                if (lobAndRefFields.contains(fieldName)) {
                    return new GetterVisitor(mv, fieldName, typeName);
                }
                return mv;
            }

            private MethodVisitor asSetter(String fieldName, int access, String name, String desc, String[] exceptions,
                                           String sig) {
                MethodVisitor mv = super.visitMethod(access, name, desc, sig, exceptions);
                Type[] types = Type.getArgumentTypes(desc);
                if (fieldName.length() == 0 || types.length != 1)
                    return mv;
                if (enumFields.contains(fieldName) /*避免继承问题 && nonStaticFields.contains(fieldName)*/) {
                    return new SetterVisitor(mv, fieldName, typeName, types[0]);
                } else if (lobAndRefFields.contains(fieldName)) {
                    return new SetterOfClearLazyload(mv, fieldName, typeName);
                } else {
                    String altFieldName = "is" + SpringStringUtils.capitalize(fieldName);
                    //特定情况，当boolean类型并且field名称是isXXX，setter是setXXX()
                    if (enumFields.contains(altFieldName)) {
                        return new SetterVisitor(mv, altFieldName, typeName, types[0]);
                    }
                }
                return mv;
            }

        }, 0);
        return cw.toByteArray();
    }

    private boolean isEntityClass(String[] interfaces, String superName, boolean defaultValue) {
        // sf/database/DataObject
        String clzPath = DBObject.class.getName().replace(".", "/");
        if (clzPath.equals(superName)) {
            // 绝大多数实体都是继承这个类的
            return true;
        }

        // Lsf/database/IDBEntity;
        String objectToFindDBEntity = "L" + IDBEntity.class.getName().replace(".", "/") + ";";
        if (ArrayUtils.contains(interfaces, objectToFindDBEntity)) {
            return true;
        }
        if ("java/lang/Object".equals(superName)) {
            return false;
        }

        // 递归检查父类
        ClassReader cl = null;
        try {
            URL url = ClassLoader.getSystemResource(superName + ".class");
            if (url == null && root != null) {
                if (root != null) {
                    Resource r = root.getResource(superName + ".class");
                    url = r.getURL();
                }
            }
            if (url == null) { //父类找不到，无法准确判断
                return defaultValue;
            }
            if (url == null) { // 父类找不到，无法准确判断
                return defaultValue;
            }
            byte[] parent = IOUtils.toByteArray(url);
            cl = new ClassReader(parent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cl != null) {
            return isEntityClass(cl.getInterfaces(), cl.getSuperName(), defaultValue);
        }
        return false;
    }

    // public byte[] getBinaryData_x();
    // Code:
    // 0: aload_0
    // 1: ldc #117; //String binaryData
    // 3: invokevirtual #118; //Method beforeGet:(Ljava/lang/String;)V
    // 6: aload_0
    // 7: getfield #121; //Field binaryData:[B
    // 10: areturn
    static class GetterVisitor extends MethodVisitor implements Opcodes {
        private String name;
        private String typeName;

        public GetterVisitor(MethodVisitor mv, String name, String typeName) {
            super(ASM5, mv);
            this.name = name;
            this.typeName = typeName;
        }

        @Override
        public void visitCode() {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "beforeGet", "(Ljava/lang/String;)V", false);
            super.visitCode();
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(2, maxLocals);
        }

        // 去除本地变量表。
        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        }
    }

    static class SetterOfClearLazyload extends MethodVisitor implements Opcodes {
        private String name;
        private String typeName;

        public SetterOfClearLazyload(MethodVisitor mv, String name, String typeName) {
            super(Opcodes.ASM5, mv);
            this.name = name;
            this.typeName = typeName;
        }

        // 去除本地变量表。否则生成的类用jd-gui反编译时，添加的代码段无法正常反编译
        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        }

        @Override
        public void visitCode() {
            mv.visitIntInsn(ALOAD, 0);
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "beforeSet", "(Ljava/lang/String;)V", false);
            super.visitCode();
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(4, maxLocals);
        }
    }

    // public void setBytes(byte[]);
    // descriptor: ([B)V
    // flags: ACC_PUBLIC
    // Code:
    // stack=3, locals=2, args_size=2
    // 0: aload_0
    // 1: getfield #227 // Field _recordUpdate:Z
    // 4: ifeq 15
    // 7: aload_0
    // 8: getstatic #230 // Field
    // sf/db/domain/User$Field.bytes:Lsf/db/domain/User$Field;
    // 11: aload_1
    // 12: invokevirtual #235 // Method
    // prepareUpdate:(Lsf/database/DBField;Ljava/lang/Object;)V
    // 15: aload_0
    // 16: aload_1
    // 17: putfield #223 // Field bytes:[B
    // 20: return
    // LineNumberTable:
    // line 239: 0
    // line 240: 7
    // line 242: 15
    // line 243: 20
    // LocalVariableTable:
    // Start Length Slot Name Signature
    // 0 21 0 this Lsf/db/domain/User;
    // 0 21 1 bytes [B
    // StackMapTable: number_of_entries = 1
    // frame_type = 15 /* same */

    static class SetterVisitor extends MethodVisitor implements Opcodes {
        private String name;
        private String typeName;
        private Type paramType;

        public SetterVisitor(MethodVisitor mv, String name, String typeName, Type paramType) {
            super(ASM5, mv);
            this.name = name;
            this.typeName = typeName;
            this.paramType = paramType;
        }

        // 去除本地变量表。否则生成的类用jd-gui反编译时，添加的代码段无法正常反编译
        @Override
        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        }

        // @Override
        // public void visitLineNumber(int line, Label start) {
        // // 清空
        // }

        @Override
        public void visitCode() {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, typeName, "_recordUpdate", "Z");
            Label norecord = new Label();
            mv.visitJumpInsn(IFEQ, norecord);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, typeName + "$Field", name, "L" + typeName + "$Field;");

            // if (paramType.isPrimitive() ){
            if (paramType.getSort() != Type.OBJECT && paramType.getSort() != Type.ARRAY) {
                mv.visitVarInsn(ASMUtils.getLoadIns(paramType), 1);
                ASMUtils.doWrap(mv, paramType);
            } else {
                mv.visitVarInsn(ALOAD, 1);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "prepareUpdate", "(Lsf/database/DBField;Ljava/lang/Object;)V",
                    false);

            mv.visitLabel(norecord);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            super.visitCode();

        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(4, maxLocals);
        }

    }

}
