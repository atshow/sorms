package sf.database.jdbc.sql;

import java.util.Iterator;

public interface OrmIterator<T> {
    void dealWith(Iterator<T> it);
}
