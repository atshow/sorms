package sf.database.mapper;

import sf.database.dao.DBClient;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认Java代理实现.
 * @author
 */
public class DefaultMapperBuilder implements MapperBuilder {

    /**
     * The cache.
     */
    protected Map<Class<?>, Object> cache = new ConcurrentHashMap<Class<?>, Object>();

    /**
     * The sql manager.
     */
    protected DBClient sqlManager;


    /**
     * The Constructor.
     * @param sqlManager the sql manager
     */
    public DefaultMapperBuilder(DBClient sqlManager) {
        super();
        this.sqlManager = sqlManager;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.beetl.sql.ext.dao2.MapperBuilder#getMapper(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> mapperInterface) {
        if (cache.containsKey(mapperInterface)) {
            return (T) cache.get(mapperInterface);
        } else {
            T instance = this.buildInstance(mapperInterface);
            cache.put(mapperInterface, instance);
            return instance;
        }
    }

    /**
     * Builds the instance.
     * @param <T>             the generic type
     * @param mapperInterface the dao2 interface
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <T> T buildInstance(Class<T> mapperInterface) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        //使用ContextLoader，适合大多数框架
        return (T) Proxy.newProxyInstance(loader == null ? this.getClass().getClassLoader() : loader, new Class<?>[]{mapperInterface},
                new MapperJavaProxy(this, sqlManager, mapperInterface));
    }
}
