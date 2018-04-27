package sf.database.dbinfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DBInfo {
    static final Map<String, TableInfo> tablePool = new ConcurrentHashMap<String, TableInfo>(32);
}
