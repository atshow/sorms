package sf.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 延迟加载执行句柄
 * @author
 */
public interface LazyLoadTask {

    void process(Connection db, Object obj) throws SQLException;

    Collection<String> getEffectFields();
}
