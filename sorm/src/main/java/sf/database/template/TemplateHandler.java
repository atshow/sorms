package sf.database.template;

import sf.database.template.sql.SQLContext;

import java.util.Map;

public interface TemplateHandler {
    void loadAllSQL();

    String getSQL(String sqlId);

    SQLContext getParsedSql(String sqlId, Map<String, Object> paramters);
}
