package sf.database.jdbc.sql;

import sf.jooq.JooqImpl;
import sf.jooq.JooqInf;
import sf.querydsl.QueryDSLInf;
import sf.querydsl.QueryDSLOrmImpl;

/**
 * 基础sql封装类
 * @author shixiafeng
 */
public class Crud {
    private static Crud c = null;

    private CrudSqlInf crudSql = CrudSqlImpl.getInstance();

    private CrudTemplateInf crudTemplate = new CrudTemplateImpl();

    private CrudModelInf crudModel = new CrudModelImpl();

    private QueryDSLInf queryDSLInf = new QueryDSLOrmImpl();

    private JooqInf jooqInf = new JooqImpl();

    private Crud() {

    }

    public static Crud getInstance() {
        if (c == null) {
            c = new Crud();
        }
        return c;
    }

    /**
     * 直接对sql操作
     * @return
     */
    public CrudSqlInf getCrudSql() {
        return crudSql;
    }

    /**
     * 直接对模板sql操作
     * @return
     */
    public CrudTemplateInf getCrudTemplate() {
        return crudTemplate;
    }

    public CrudModelInf getCrudModel() {
        return crudModel;
    }

    /**
     * 获取dsljdbc的接口
     * @return
     */
    public QueryDSLInf getQueryDSLInf() {
        return queryDSLInf;
    }

    public JooqInf getJooqInf() {
        return jooqInf;
    }
}
