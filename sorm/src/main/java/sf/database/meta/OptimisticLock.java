package sf.database.meta;

import sf.database.DBField;
import sf.database.DBObject;
import sf.database.OrmContext;
import sf.database.OrmParameter;
import sf.database.jdbc.sql.Crud;
import sf.database.util.OrmValueUtils;
import sf.tools.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 乐观锁支持
 * @see javax.persistence.Version
 */
public class OptimisticLock {
    /**
     * @param conn
     * @param obj
     * @param <T>
     * @see javax.persistence.Version
     * 从数据库中获取最新的乐观锁的值到对象中,一般情况下,该方法用不到.慎用.
     */
    public static <T extends DBObject> void setNewOptimisticLockValues(Connection conn, T obj) throws SQLException {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<String> fStr = new ArrayList<>();
        if (!table.getVersionMap().isEmpty()) {
            for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                ColumnMapping cm = e.getValue();
                String columnName = cm.getRawColumnName();
                fStr.add(columnName);
            }
            OrmContext c = new OrmContext(table);
            List<ColumnMapping> pKeys = table.getPkFields();
            List<OrmParameter> parameters = new ArrayList<>();
            StringBuilder sql = new StringBuilder();
            sql.append("select ").append(StringUtils.join(fStr, ",")).append(" from ").append(table.getTableName()).append(" where ");
            boolean f = false;
            for (ColumnMapping pk : pKeys) {
                sql.append(f ? " and " : "").append(pk.getRawColumnName()).append(" =? ");
                Object value = OrmValueUtils.getValue(obj, pk);
                parameters.add(new OrmParameter(value, pk));
                f = true;
            }
            c.setSql(sql.toString());
            c.setParas(parameters);

            DBObject temp = Crud.getInstance().getCrudModel().selectOne(conn, obj.getClass(), c);
            if (temp != null) {
                //设置最新的版本值到obj对象中
                for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                    ColumnMapping cm = e.getValue();
                    Object value = OrmValueUtils.getValue(temp, cm);
                    cm.getFieldAccessor().set(obj, value);
                }
            }
        }
    }
}
