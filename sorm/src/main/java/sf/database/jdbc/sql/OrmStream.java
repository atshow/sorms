package sf.database.jdbc.sql;

import java.util.stream.Stream;

public interface OrmStream<T> {
    void dealWith(Stream<T> stream);
}
