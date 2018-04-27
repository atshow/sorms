package sf.dsl;

import sf.database.DBObject;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;

public final class QB {
    /**
     * 创建一个查询请求,当不赋任何条件的时候.
     * @param clz 要查询的表
     * @return
     */
    public static <T extends DBObject> Query<T> create(Class<T> clz) {
        TableMapping meta = MetaHolder.getMeta(clz);
        QueryImpl<T> query = new QueryImpl<>(clz);
        return query;
    }
}
