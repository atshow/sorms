package sf.database.mapper;

import sf.database.dao.DBClient;

import java.lang.reflect.Method;

/**
 * @author xiandafu
 */
public interface MapperInvoke {
    Object call(DBClient sm, Class entityClass, String sqlId, Method m, Object[] args);
}
