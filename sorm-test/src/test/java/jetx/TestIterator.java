package jetx;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.google.common.base.Stopwatch;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import sf.tools.reflect.UnsafeUtils;
import sun.misc.Unsafe;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class TestIterator {
    private long times = 100_000_000L;
    private SimpleBean bean;
    private ComBean cb;
    private String formatter = "%s %d times using %d ms";

    @Before
    public void setUp() throws Exception {
        bean = new SimpleBean();
        bean.setName("haoyifen");
        cb = new ComBean();
        cb.setId("id");
    }

    //直接通过Java的get方法
    @Test
    public void directGet() {
        String s = null;
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            int age = bean.getAge();
//            s = bean.name;
//            bean.setName("1");
        }
        watch.stop();
        String result = String.format(formatter, "directGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    @Test
    public void directSet() {
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            bean.setCb(cb);
        }
        watch.stop();
        String result = String.format(formatter, "directSet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    //通过高性能的ReflectAsm库进行测试，仅进行一次methodAccess获取
    @Test
    public void reflectAsmGet() {
        MethodAccess methodAccess = MethodAccess.get(SimpleBean.class);
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            methodAccess.invoke(bean, "getName");
//            methodAccess.invoke(bean, "setName","1");
        }
        watch.stop();
        String result = String.format(formatter, "reflectAsmGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    @Test
    public void reflectAsmSet() {
        MethodAccess methodAccess = MethodAccess.get(SimpleBean.class);
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            methodAccess.invoke(bean, "setCb", cb);
        }
        watch.stop();
        String result = String.format(formatter, "reflectAsmSet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    public static Unsafe getUnsafe() {
        Field f = null;
        try {
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            return unsafe;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void unsafeGet() throws NoSuchFieldException {
        bean.setName("1");
        Unsafe unsafe = getUnsafe();
        Field f = SimpleBean.class.getDeclaredField("age");

        long j = unsafe.objectFieldOffset(f);
        Object obj = UnsafeUtils.getValue(bean, j, String.class);
        Stopwatch watch = Stopwatch.createStarted();
        String s = null;
        for (long i = 0; i < times; i++) {
//             obj = unsafe.getObject(bean, j);
//            int l = UnsafeUtils.getValue(bean, j, int.class);
            obj = UnsafeUtils.getValue(bean, j, String.class);
        }
        watch.stop();
        String result = String.format(formatter, "unsafeGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    @Test
    public void unsafeSet() throws NoSuchFieldException {
        bean.setName("1");
        Unsafe unsafe = getUnsafe();
        Field f = SimpleBean.class.getDeclaredField("name");
        Stopwatch watch = Stopwatch.createStarted();
        long j = unsafe.objectFieldOffset(f);
        String s = null;
        Integer t = new Integer(1);
        for (long i = 0; i < times; i++) {
//            unsafe.putObject(bean,j,"1");
            UnsafeUtils.setValue(bean, j, String.class, "1");
        }
        watch.stop();
        String result = String.format(formatter, "unsafeSet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    //通过Java Class类自带的反射获得Method测试，仅进行一次method获取
    @Test
    public void javaReflectGet() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method getName = SimpleBean.class.getMethod("getName");
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            getName.invoke(bean);
        }
        watch.stop();
        String result = String.format(formatter, "javaReflectGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    @Test
    public void javaReflectGet2() throws IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        Field f = SimpleBean.class.getDeclaredField("name");
        f.setAccessible(true);
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            f.get(bean);
        }
        watch.stop();
        String result = String.format(formatter, "javaReflectGet2", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    //使用Java自带的Property属性获取Method测试，仅进行一次method获取
    @Test
    public void propertyGet() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, IntrospectionException {
        Method method = null;
        BeanInfo beanInfo = Introspector.getBeanInfo(SimpleBean.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals("name")) {
                method = propertyDescriptor.getReadMethod();
                break;
            }
        }
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {

            method.invoke(bean);
        }
        watch.stop();
        String result = String.format(formatter, "propertyGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }

    //BeanUtils的getProperty测试
    @Test
    public void beanUtilsGet() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Stopwatch watch = Stopwatch.createStarted();
        for (long i = 0; i < times; i++) {
            BeanUtils.getPropertyDescriptor(bean.getClass(), "name");
        }
        watch.stop();
        String result = String.format(formatter, "beanUtilsGet", times, watch.elapsed(TimeUnit.MILLISECONDS));
        System.out.println(result);
    }
}