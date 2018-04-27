package sf.database.jdbc.sql;

import sf.common.wrapper.Page;
import sf.database.template.TemplateRender;
import sf.database.template.sql.SQLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 根据模板id的sql查询
 * @author sxf
 */
public class CrudTemplateImpl implements CrudTemplateInf {

    private CrudSqlInf cs = CrudSqlImpl.getInstance();

    @Override
    public int[] executeBatch(Connection conn, String sqlId, List<Map<String, Object>> parameters) throws SQLException {
        List<Object[]> paras = new ArrayList<>();
        SQLContext one = null;
        if (parameters != null) {
            for (Map<String, Object> map : parameters) {
                SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, map);
                if (one == null) {
                    one = parsedSql;
                }
                paras.add(parsedSql.getValues().toArray());
            }
        }
        if (one == null) {
            if (!parameters.isEmpty()) {
                one = TemplateRender.getTemplateHandler().getParsedSql(sqlId, parameters.get(0));
            } else {
                one = TemplateRender.getTemplateHandler().getParsedSql(sqlId, null);
            }
        }
        return cs.executeBatch(conn, one.getAfterSql(), paras);
    }

    @Override
    public int execute(Connection conn, String sqlId, Map<String, Object> paramters) throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.execute(conn, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

    @Override
    public <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, String sqlId,
                                  Map<String, Object> paramters) throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.selectPage(conn, start, limit, beanClass, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

    @Override
    public <T> T[] selectArray(Connection conn, Class<T> arrayComponentClass, String sqlId,
                               Map<String, Object> paramters) throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.selectArray(conn, arrayComponentClass, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

    @Override
    public <T> List<T> selectList(Connection conn, Class<T> beanClass, String sqlId, Map<String, Object> paramters)
            throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.selectList(conn, beanClass, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

    @Override
    public <T> T selectOne(Connection conn, Class<T> beanClass, String sqlId, Map<String, Object> paramters)
            throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.selectOne(conn, beanClass, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

    @Override
    public List<Map<String, Object>> select(Connection conn, String sqlId, Map<String, Object> paramters)
            throws SQLException {
        SQLContext parsedSql = TemplateRender.getTemplateHandler().getParsedSql(sqlId, paramters);
        return cs.select(conn, parsedSql.getAfterSql(), parsedSql.getValues().toArray());
    }

}
