package sf.jooq;

import org.jooq.Configuration;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.JooqRecord;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.util.OrmValueUtils;
import sf.jooq.tables.JooqTable;

import java.util.ArrayList;
import java.util.List;

public class JooqDao extends DAOImpl {
    private Class<?> pojo;
    private JooqTable jooqTable;

    private JooqDao(Table<JooqRecord> table, Class<?> type) {
        super(table, type);
    }

    public JooqDao(Table<JooqRecord> table, Class<?> type, Configuration configuration) {
        super(table, type, configuration);
        pojo = type;
        jooqTable = (JooqTable) table;
    }

    @Override
    protected Record getId(Object object) {
        List<Object> values = new ArrayList<>();
        TableMapping tm = MetaHolder.getMeta(object.getClass());
        for (ColumnMapping pk : tm.getPkFields()) {
            values.add(OrmValueUtils.getValue(object, pk));
        }
        return (Record) compositeKeyRecord(values.toArray());
    }

    public Class<?> getPojo() {
        return pojo;
    }

    public void setPojo(Class<?> pojo) {
        this.pojo = pojo;
    }
}
