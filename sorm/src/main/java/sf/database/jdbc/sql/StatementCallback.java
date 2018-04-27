package sf.database.jdbc.sql;

import java.sql.Statement;
import java.util.List;

public interface StatementCallback {
    void setValues(Statement s, List<Object> values);
}
