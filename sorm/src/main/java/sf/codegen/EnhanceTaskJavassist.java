package sf.codegen;

import javassist.*;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import sf.database.DBObject;
import sf.database.IDBEntity;
import sf.tools.ArrayUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.SpringStringUtils;

import javax.persistence.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnhanceTaskJavassist {

    public EnhanceTaskJavassist() {
    }

    /**
     * @param className
     * @return 返回null表示不需要增强，返回byte[0]表示该类已经增强，返回其他数据为增强后的class
     * @throws Exception
     */
    public byte[] doEnhance(ClassPool pool, String className) throws Exception {
        Assert.notNull(className, "");
        try {
            byte[] data = enhanceClass(pool, className);
            return data;
        } catch (EnhancedException e) {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    private static class EnhancedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public byte[] enhanceClass(ClassPool pool, String className) throws Exception {

        byte[] b = null;
        // 获得要修改的类
        CtClass cc = pool.get(className);

        if (!cc.hasAnnotation(Table.class) && !cc.hasAnnotation(Entity.class)) {
            return null;
        }

        boolean isEntityInterface = isEntityClass(cc.getInterfaces(), cc.getSuperclass());
        if (!isEntityInterface) {
            return null;
        }

        byte[] sfd = cc.getAttribute("sfd");

        if (sfd != null && sfd.length > 0) {
            throw new EnhancedException();
        } else {
            // 设置修改标志
            sfd = new byte[]{0x1f};
            ClassFile cf = cc.getClassFile();
            AttributeInfo ai = new AttributeInfo(cc.getClassFile().getConstPool(), "sfd", sfd);
            cf.addAttribute(ai);

            // 查找Field定义的字段
            List<String> enumFieldList = new ArrayList<String>();
            CtClass[] innerCC = cc.getDeclaredClasses();
            if (innerCC != null && innerCC.length > 0) {
                for (CtClass ctClass : innerCC) {
                    CtClass[] interfeaces = ctClass.getInterfaces();
                    CtClass inter = null;
                    if (interfeaces != null && interfeaces.length > 0) {
                        inter = interfeaces[0];
                    }
                    if (ctClass.isEnum() && inter != null && inter.getName().contains("sf.database.DBField")) {
                        for (CtField ctf : ctClass.getDeclaredFields()) {
                            enumFieldList.add(ctf.getName());
                        }
                    }
                }
            }


            // 设置方法需要的参数(自动查找父类的)
            List<CtField> ctFields = new ArrayList<>();
            CtField[] cfs = null;
            CtClass superClass = cc;
            while (superClass != null && !DBObject.class.getName().equals(superClass.getName())) {
                cfs = superClass.getDeclaredFields();
                ctFields.addAll(Arrays.asList(cfs));
                superClass = superClass.getSuperclass();
            }

            CtMethod[] ctms = cc.getDeclaredMethods();
            if (ctms != null && ctms.length > 0) {
                for (CtField ctf : ctFields) {
                    String mName = ctf.getName();
                    String mtName = SpringStringUtils.capitalize(mName);
                    if (ctf.hasAnnotation(Column.class) || ctf.hasAnnotation(Id.class) || enumFieldList.contains(mName)) {
                        for (CtMethod ctm : ctms) {
                            if (ctm.getName().equals("set" + mtName) && !Modifier.isStatic(ctm.getModifiers())
                                    && enumFieldList.contains(mName)) {

                                String wraperd = "($w)$1";
                                CtClass innct = ctm.getParameterTypes()[0];
                                String wrapperName = "";
                                if (innct.isPrimitive()) {
                                    // 是否是原始类型
                                    CtPrimitiveType ctpt = (CtPrimitiveType) innct;
                                    wrapperName = ctpt.getWrapperName();
                                    wraperd = wrapperName + ".valueOf($1)";
                                }

                                String body = "{if (this._recordUpdate) { prepareUpdate(" + cc.getName() + ".Field."
                                        + ctf.getName() + ", " + wraperd + "); } this." + ctf.getName() + "=$1;}";
                                // ctm.setBody(body);

                                String src = "if (this._recordUpdate) {prepareUpdate(" + cc.getName() + ".Field."
                                        + ctf.getName() + ",  " + wraperd + ");}";
                                ctm.insertBefore(src);
                                break;
                            }
                        }
                    } else {
                        boolean cascade = ctf.hasAnnotation(OneToMany.class) || ctf.hasAnnotation(ManyToOne.class) ||
                                ctf.hasAnnotation(ManyToMany.class) || ctf.hasAnnotation(OneToOne.class);
                        if (cascade) {
                            for (CtMethod ctm : ctms) {
                                if (ctm.getName().equals("set" + mtName) && !Modifier.isStatic(ctm.getModifiers())) {
                                    String src = "beforeSet(\"" + mName + "\");";
                                    ctm.insertBefore(src);
                                } else if (ctm.getName().equals("get" + mtName) && !Modifier.isStatic(ctm.getModifiers())) {
                                    String src = "beforeGet(\"" + mName + "\");";
                                    ctm.insertBefore(src);
                                }
                            }
                        }
                    }
                }
            }
            b = cc.toBytecode();
            cc.detach();// 卸载
        }
        return b;
    }

    private boolean isEntityClass(CtClass[] interfaces, CtClass superName) {
        if (DBObject.class.getName().equals(superName.getName()))
            // 绝大多数实体都是继承这个类的
            return true;

        if (interfaces != null) {
            for (CtClass ct : interfaces) {
                if (IDBEntity.class.getName().equals(ct.getName())) {
                    return true;
                }
            }
        }

        if ("java.lang.Object".equals(superName.getName())) {
            return false;
        }
        // 递归检查父类
        CtClass cl = superName;
        if (cl != null) {
            try {
                return isEntityClass(cl.getInterfaces(), cl.getSuperclass());
            } catch (NotFoundException e) {
                return false;
            }
        }
        return false;
    }

    private static ClassLoader getLocaleClassLoader(URL path) throws Exception {
        // 实例化类加载器
        return new URLClassLoader(new URL[]{path});
    }
}
