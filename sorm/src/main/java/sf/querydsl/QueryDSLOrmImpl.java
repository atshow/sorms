package sf.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.*;
import sf.common.wrapper.Page;
import sf.database.DBObject;
import sf.database.OrmContext;
import sf.database.jdbc.sql.Crud;
import sf.database.util.DBUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryDSLOrmImpl implements QueryDSLInf {
    @Override
    @Deprecated
    public <T> SQLQuery<T> sqlQuery(Connection conn, Class<? extends DBObject>... tableClass) {
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        //注入自定义类型
        OrmSQLQuery<T> query = new OrmSQLQuery<T>(conn, sqlTemplates);
        Configuration configuration = query.getConfiguration();
        if (tableClass != null && tableClass.length > 0) {
            for (Class<?> clz : tableClass) {
                QueryDSL.setReturnType(clz, configuration);
            }
        }
        return query;
    }

    @Override
    public <T> List<T> sqlQueryList(Connection conn, AbstractSQLQuery query, Class<T> returnClass) throws SQLException {
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration c = QueryDSL.getConfigurationQuery(query);
        c.setTemplates(sqlTemplates);
        OrmContext context = new OrmContext();
        //是否是静态model
        boolean staticModel = QueryDSL.getOrmContext(query, context);
        Class<T> clz = returnClass;
        if (clz == null) {
            clz = getReturnClz(query);
        }
        if (staticModel) {
            return Crud.getInstance().getCrudModel().selectList(conn, clz, context);
        } else {
            return Crud.getInstance().getCrudSql().selectList(conn, clz, context.getSql(), context.getValues());
        }
    }

    @Override
    public <T> Page<T> sqlQueryPage(Connection conn, AbstractSQLQuery query, Class<T> returnClass, int start, int limit) throws SQLException {
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration c = QueryDSL.getConfigurationQuery(query);
        c.setTemplates(sqlTemplates);
        OrmContext context = new OrmContext();
        boolean staticModel = QueryDSL.getOrmContext(query, context);
        Class<T> clz = returnClass;
        if (clz == null) {
            clz = getReturnClz(query);
        }
        if (staticModel) {
            return Crud.getInstance().getCrudModel().selectPage(conn, start, limit, clz, context);
        } else {
            return Crud.getInstance().getCrudSql().selectPage(conn, start, limit, clz, context.getSql(), context.getValues());
        }
    }

    @Override
    public <T> T sqlQueryOne(Connection conn, AbstractSQLQuery query, Class<T> returnClass) throws SQLException {
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration c = QueryDSL.getConfigurationQuery(query);
        c.setTemplates(sqlTemplates);
        OrmContext context = new OrmContext();
        boolean staticModel = QueryDSL.getOrmContext(query, context);
        Class<T> clz = returnClass;
        if (clz == null) {
            clz = getReturnClz(query);
        }
        if (staticModel) {
            return Crud.getInstance().getCrudModel().selectOne(conn, clz, context);
        } else {
            return Crud.getInstance().getCrudSql().selectOne(conn, clz, context.getSql(), context.getValues());
        }
    }

    @Override
    public int queryDSLInsert(Connection conn, SQLInsertClause insert) throws SQLException {
        return queryDSLSQLClause(conn, insert);
    }

    @Override
    public int queryDSLUpdate(Connection conn, SQLUpdateClause update) throws SQLException {
        return queryDSLSQLClause(conn, update);
    }

    @Override
    public int queryDSLDelete(Connection conn, SQLDeleteClause delete) throws SQLException {
        return queryDSLSQLClause(conn, delete);
    }

    @Override
    public int queryDSLMerge(Connection conn, SQLMergeClause merge) throws SQLException {
        return queryDSLSQLClause(conn, merge);
    }

    private int queryDSLSQLClause(Connection conn, AbstractSQLClause dml) throws SQLException {
        SQLTemplates sqlTemplates = DBUtils.doGetDialect(conn, false).getQueryDslDialect();
        Configuration c = QueryDSL.getConfigurationDML(dml);
        c.setTemplates(sqlTemplates);
        List<SQLBindings> sqlBindings = dml.getSQL();
        List<Object[]> objects = new ArrayList<>();
        String sql = null;
        for (SQLBindings b : sqlBindings) {
            if (sql == null) {
                sql = b.getSQL();
            }
            objects.add(b.getNullFriendlyBindings().toArray());
        }
        int[] counts = Crud.getInstance().getCrudSql().executeBatch(conn, sql, objects);
        int count = 0;
        for (int i : counts) {
            count += i;
        }
        return count;
    }

    private Class getReturnClz(AbstractSQLQuery query) {
        Expression<?> expression = query.getMetadata().getProjection();
        Class<?> type = expression.getType();
        Class<?> clz = Map.class;
        if (type != null && !Tuple.class.isAssignableFrom(type)) {
            clz = type;
        }
        return clz;
    }
}
