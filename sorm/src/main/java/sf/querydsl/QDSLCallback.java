package sf.querydsl;

import com.querydsl.sql.SQLQuery;

public interface QDSLCallback {
    <T> void sql(SQLQuery<T> query);
}
