package sf.jooq;

import org.jooq.*;
import org.jooq.impl.JooqVisitor;
import sf.common.wrapper.Page;
import sf.database.jdbc.sql.Crud;
import sf.database.util.DBUtils;
import sf.jooq.tables.JooqTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JooqImpl implements JooqInf {

    @Override
    public <T> T jooqSelectOne(Connection conn, Select select, Class<T> returnClass) throws SQLException {
        setDialect(conn, select);
        String sql = select.getSQL();
        List<Object> values = select.getBindValues();
        List<Field<?>> fields = select.getSelect();
        Class<?> beanClass = returnClass;
        if (beanClass == null) {
            beanClass = selectQueryReturnClass((SelectConditionStep) select, fields);
        }
        return Crud.getInstance().getCrudSql().selectOne(conn, (Class<T>) beanClass, sql, values.toArray());
    }

    @Override
    public <T> List<T> jooqSelectList(Connection conn, Select select, Class<T> returnClass) throws SQLException {
        setDialect(conn, select);
        String sql = select.getSQL();
        List<Object> values = select.getBindValues();
        List<Field<?>> fields = select.getSelect();
        Class<?> beanClass = returnClass;
        if (beanClass == null) {
            beanClass = selectQueryReturnClass((SelectConditionStep) select, fields);
        }
        return Crud.getInstance().getCrudSql().selectList(conn, (Class<T>) beanClass, sql, values.toArray());
    }

    @Override
    public <T> Page<T> jooqSelectPage(Connection conn, Select select, Class<T> returnClass, int start, int limit) throws SQLException {
        setDialect(conn, select);
        String sql = select.getSQL();
        List<Object> values = select.getBindValues();
        List<Field<?>> fields = select.getSelect();
        Class<?> beanClass = returnClass;
        if (beanClass == null) {
            beanClass = selectQueryReturnClass((SelectConditionStep) select, fields);
        }
        return Crud.getInstance().getCrudSql().selectPage(conn, start, limit, (Class<T>) beanClass, sql, values.toArray());
    }

    private void setDialect(Connection conn, Attachable select) {
        if (select.configuration() == null) {
            throw new RuntimeException("未找到配置!");
        } else {
            SQLDialect sqlDialect = DBUtils.doGetDialect(conn, false).getJooqDialect();
            if (select.configuration().dialect() != sqlDialect) {
                //需要从连接中动态获取dialect
                select.configuration().set(sqlDialect);
            }
        }
    }

    /**
     * @param select
     * @param fields
     * @return
     */
    private Class<?> selectQueryReturnClass(SelectConditionStep select, List<Field<?>> fields) {
        Class<?> beanClass = Map.class;
        SelectQuery query = select.getQuery();
        List<? extends Table<?>> fromTables = JooqVisitor.getSelectFromTableList(query);
        if (!fields.isEmpty()) {
            //是否相同
            boolean same = true;
            TableField f = (TableField) fields.get(0);
            Table table = f.getTable();
            for (Field tf : fields) {
                TableField temp = (TableField) tf;
                if (!Objects.equals(f.getTable(), temp.getTable())) {
                    same = false;
                    break;
                }
            }
            if (same) {
                if (fields.size() == 1) {
                    //单一值返回
                    beanClass = Object.class;
                } else {
                    if (JooqTable.class.isAssignableFrom(table.getClass())) {
                        beanClass = ((JooqTable) table).getClz();
                    }
                }
            }
        } else if (fromTables.size() == 1) {
            Table table = fromTables.get(0);
            if (JooqTable.class.isAssignableFrom(table.getClass())) {
                beanClass = ((JooqTable) table).getClz();
            }
        }
        return beanClass;
    }

    @Override
    public int jooqInsert(Connection conn, Insert insert) throws SQLException {
        setDialect(conn, insert);
        String sql = insert.getSQL();
        List<Object> values = insert.getBindValues();
        return Crud.getInstance().getCrudSql().execute(conn, sql, values.toArray());
    }

    @Override
    public int jooqUpdate(Connection conn, Update update) throws SQLException {
        setDialect(conn, update);
        String sql = update.getSQL();
        List<Object> values = update.getBindValues();
        return Crud.getInstance().getCrudSql().execute(conn, sql, values.toArray());
    }

    @Override
    public int jooqDelect(Connection conn, Delete delete) throws SQLException {
        setDialect(conn, delete);
        String sql = delete.getSQL();
        List<Object> values = delete.getBindValues();
        return Crud.getInstance().getCrudSql().execute(conn, sql, values.toArray());
    }
}
