package sf.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.JooqVisitor;
import sf.jooq.tables.JooqTable;
import sf.tools.StringUtils;

import java.util.List;

public class Jooq {
    public static Class<?> getSelectFromTable(Select select) {
        SelectQuery query = ((SelectWhereStep) select).getQuery();
        Class<?> tableClz = null;
        List<? extends Table<?>> fromTables = JooqVisitor.getSelectFromTableList(query);
        if (fromTables.size() == 1) {
            Table table = fromTables.get(0);
            tableClz = ((JooqTable) table).getClz();
        }
        return tableClz;
    }


    public static Schema getSchema(String catalog, String schema) {
        if (StringUtils.isNotBlank(schema)) {
            if (catalog == null) {
                return DSL.schema(DSL.name(schema));
            } else {
                return DSL.schema(DSL.name(catalog, schema));
            }
        }
        return null;
    }
}
