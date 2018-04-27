package sf.database.mapper;

import sf.database.dao.DBClient;
import sf.dsl.annotation.DSLSelect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Java代理实现.
 * <p>
 * <a href="http://git.oschina.net/xiandafu/beetlsql/issues/54"># 54</a>
 * 封装sqlmanager
 * </p>
 * @author zhoupan, xiandafu
 */
public class MapperJavaProxy implements InvocationHandler {

    /**
     * The sql manager.
     */
    protected DBClient sqlManager;

    /**
     * The entity class.
     */
    protected Class<?> entityClass;

    protected DefaultMapperBuilder builder;

    private Class mapperInterface;


    protected DaoMapper mapperConfig;

    /**
     * The Constructor.
     */
    public MapperJavaProxy() {

    }

    /**
     * @param builder
     * @param sqlManager
     * @param mapperInterface
     */
    public MapperJavaProxy(DefaultMapperBuilder builder, DBClient sqlManager, Class<?> mapperInterface) {
        super();
        this.sqlManager = sqlManager;
        this.builder = builder;
        this.mapperInterface(mapperInterface);
        this.mapperInterface = mapperInterface;
    }


    /**
     * Mapper interface.
     * @param mapperInterface the dao2 interface
     * @return the dao2 proxy
     */
    public MapperJavaProxy mapperInterface(Class<?> mapperInterface) {
        this.onResolveEntityClassFromMapperInterface(mapperInterface);
        return this;
    }


    /**
     * Entity class.
     * @param entityClass the entity class
     * @return the dao2 proxy
     */
    public MapperJavaProxy entityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
        return this;
    }

    /**
     * Check args.
     */
    protected void checkArgs() {
    }

    /**
     * Builds the.
     * @return the dao2 proxy
     */
    public MapperJavaProxy build() {
        this.checkArgs();
        return this;
    }

    /**
     * 获取BaseMapper&lt;EntityClass&gt;接口的泛型实体参数类.
     * @param mapperInterface the dao2 interface
     */
    protected void onResolveEntityClassFromMapperInterface(Class<?> mapperInterface) {
        if (mapperInterface.isInterface()) {
            Type[] faces = mapperInterface.getGenericInterfaces();
            if (faces.length > 0 && faces[0] instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) faces[0];
                if (pt.getActualTypeArguments().length > 0) {
                    this.entityClass = (Class<?>) pt.getActualTypeArguments()[0];

                }
            }
        } else {
            throw new IllegalArgumentException("mapperInterface is not interface.");
        }
    }


    /**
     * Invoke.
     * @param proxy  the proxy
     * @param method the method
     * @param args   the args
     * @return the object
     * @throws Throwable the throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class caller = method.getDeclaringClass();
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            return "BeetlSql Mapper " + mapperInterface;
        }
        DSLSelect select = method.getAnnotation(DSLSelect.class);

        String sqlId = null;
        if (select != null) {
            String preffix = select.value();
            String name = method.getName();
            sqlId = preffix + "." + name;
        }

        Object o = null;
        //TODO
        if (caller == DaoMapper.class) {
            //内置的方法，直接调用Invoke
            Method[] declareMethods = DaoMapperImpl.class.getDeclaredMethods();

            DaoMapperImpl dmi = new DaoMapperImpl();

            for (Method m : declareMethods) {
                if (methodName.equals(m.getName())) {
                    o = m.invoke(dmi, args);
                }
            }
        } else {
            //解析方法以及注解，找到对应的处理类
        }
        return o;
    }

    public String toString() {
        return " Proxy";
    }


}
