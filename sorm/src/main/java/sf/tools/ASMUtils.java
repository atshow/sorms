package sf.tools;


import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import sf.tools.reflect.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class ASMUtils {

    public static boolean isAndroid(String vmName) {
        if (vmName == null) { // default is false
            return false;
        }

        String lowerVMName = vmName.toLowerCase();

        return lowerVMName.contains("dalvik") //
                || lowerVMName.contains("lemur") // aliyun-vm name
                ;
    }

    public static boolean isAndroid() {
        return isAndroid(System.getProperty("java.vm.name"));
    }

    public static String getDesc(Method method) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        java.lang.Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    public static String getDesc(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        } else if (returnType.isArray()) {
            return "[" + getDesc(returnType.getComponentType());
        } else {
            return "L" + getType(returnType) + ";";
        }
    }

    public static String getType(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(parameterType.getComponentType());
        } else {
            if (!parameterType.isPrimitive()) {
                String clsName = parameterType.getName();
                return clsName.replace('.', '/');
            } else {
                return getPrimitiveLetter(parameterType);
            }
        }
    }

    public static String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type: " + type.getCanonicalName() + " is not a primitive type");
    }

    public static java.lang.reflect.Type getMethodType(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName);

            return method.getGenericReturnType();
        } catch (Exception ex) {
            return null;
        }
    }

    public static java.lang.reflect.Type getFieldType(Class<?> clazz, String fieldName) {
        Class<?> clz = clazz;
        while (clz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                return field.getGenericType();
            } catch (Exception ex) {
            }
            clz = clz.getSuperclass();
        }
        return null;
    }

    public static boolean checkName(String name) {
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c < '\001' || c > '\177') {
                return false;
            }
        }
        return true;
    }

    /**
     * get the class object of primitive type
     *
     * @param mw
     * @param rawType
     */
    public static void getPrimitiveType(MethodVisitor mw, Class<?> rawType) {
        Class<?> wrapClz = BeanUtils.toWrapperClass(rawType);
        mw.visitFieldInsn(Opcodes.GETSTATIC, getType(wrapClz), "TYPE", "Ljava/lang/Class;");
    }

    /**
     * 生成拆箱方法
     *
     * @param mw
     * @param primitive 原生类型
     */
    public static void doUnwrap(MethodVisitor mw, Class<?> primitive, Class<?> wrapped) {
        String name = primitive.getName() + "Value";
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, getType(wrapped), name, getMethodDesc(primitive), false);
    }

    /**
     * 生成装箱方法
     *
     * @param mw
     * @param type 原生类型
     */
    public static void doWrap(MethodVisitor mw, Class<?> type) {
        Class<?> wrapped = BeanUtils.toWrapperClass(type);
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(wrapped), "valueOf", getMethodDesc(wrapped, type), false);
    }

    public static void doWrap(MethodVisitor mw, Type paramType) {
        Class<?> w;
        switch (paramType.getSort()) {
            case Type.BOOLEAN:
                w = Boolean.class;
                break;
            case Type.BYTE:
                w = Byte.class;
                break;
            case Type.CHAR:
                w = Character.class;
                break;
            case Type.DOUBLE:
                w = Double.class;
                break;
            case Type.FLOAT:
                w = Float.class;
                break;
            case Type.INT:
                w = Integer.class;
                break;
            case Type.LONG:
                w = Long.class;
                break;
            case Type.SHORT:
                w = Short.class;
                break;
            default:
                throw new IllegalArgumentException();
        }
        mw.visitMethodInsn(Opcodes.INVOKESTATIC, getType(w), "valueOf", getMethodDesc(w, BeanUtils.toPrimitiveClass(w)),
                false);
    }

    public static int getLoadIns(Type paramType) {
        switch (paramType.getSort()) {
            case Type.BOOLEAN:
                return Opcodes.ILOAD;
            case Type.BYTE:
                return Opcodes.ILOAD;
            case Type.CHAR:
                return Opcodes.ILOAD;
            case Type.DOUBLE:
                return Opcodes.DLOAD;
            case Type.FLOAT:
                return Opcodes.FLOAD;
            case Type.INT:
                return Opcodes.ILOAD;
            case Type.LONG:
                return Opcodes.LLOAD;
            case Type.SHORT:
                return Opcodes.ILOAD;
            default:
                return Opcodes.ALOAD;
        }
    }

    public static void iconst(MethodVisitor mw, int s) {
        switch (s) {
            case 0:
                mw.visitInsn(Opcodes.ICONST_0);
                break;
            case 1:
                mw.visitInsn(Opcodes.ICONST_1);
                break;
            case 2:
                mw.visitInsn(Opcodes.ICONST_2);
                break;
            case 3:
                mw.visitInsn(Opcodes.ICONST_3);
                break;
            case 4:
                mw.visitInsn(Opcodes.ICONST_4);
                break;
            case 5:
                mw.visitInsn(Opcodes.ICONST_5);
                break;
            default:
                mw.visitIntInsn(Opcodes.BIPUSH, s);
        }
    }

    /**
     * 生成方法签名
     *
     * @param returnType
     * @param params
     * @return
     */
    public static String getMethodDesc(Class<?> returnType, Class<?>... params) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> c : params) {
            sb.append(getDesc(c));
        }
        sb.append(')');
        sb.append(getDesc(returnType));
        return sb.toString();
    }

}
