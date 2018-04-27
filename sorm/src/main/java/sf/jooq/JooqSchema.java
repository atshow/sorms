package sf.jooq;

import org.jooq.*;
import org.jooq.impl.SchemaImpl;
import sf.common.CaseInsensitiveMap;
import sf.jooq.tables.JooqTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * schema对应的表
 */
public class JooqSchema extends SchemaImpl {
    private Map<String, JooqTable> tableMap = new CaseInsensitiveMap<JooqTable>();

    public JooqSchema(String name) {
        super(name);
    }

    public JooqSchema(String name, Catalog catalog) {
        super(name, catalog);
    }

    public JooqSchema(Name name) {
        super(name);
    }

    public JooqSchema(Name name, Catalog catalog) {
        super(name, catalog);
    }

    public void addTables(JooqTable table) {
        tableMap.put(table.getName(), table);
    }

    @Override
    public List<Table<?>> getTables() {
        List<Table<?>> tables = new ArrayList<>();
        for (Map.Entry<String, JooqTable> entry : tableMap.entrySet()) {
            tables.add(entry.getValue());
        }
        return tables;
    }

    @Override
    public List<UDT<?>> getUDTs() {
        //TODO
        return super.getUDTs();
    }

    @Override
    public List<Sequence<?>> getSequences() {
        //TODO
        return super.getSequences();
    }
}
