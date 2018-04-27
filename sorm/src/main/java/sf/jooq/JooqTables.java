package sf.jooq;

import org.jooq.Record;
import sf.database.DBObject;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.jooq.tables.JooqTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JooqTables {
    private static Map<Class, JooqTable> tableMap = new ConcurrentHashMap<>();

    public static <T extends DBObject> JooqTable<?> getTable(Class<T> clz) {
        TableMapping tm = MetaHolder.getMeta(clz);
        JooqTable<?> pb = getTable(tm);
        return pb;
    }

    public static <T extends DBObject> JooqTable<?> getTable(TableMapping tm) {
        JooqTable<?> pb = tableMap.get(tm.getThisType());
        if (pb == null) {
            pb = new JooqTable(tm.getThisType());
            tableMap.put(tm.getThisType(), pb);
            tm.setJooqTable((JooqTable<Record>) pb);
        }
        return pb;
    }
}
