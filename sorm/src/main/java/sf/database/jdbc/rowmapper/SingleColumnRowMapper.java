package sf.database.jdbc.rowmapper;

import sf.database.jdbc.type.Jdbcs;
import sf.database.jdbc.type.TypeHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 单值转换,主要是基本类型,String等.
 * @param <T>
 */
public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<T> targetClass;

    public SingleColumnRowMapper(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T handle(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnType = rsmd.getColumnType(1);
        TypeHandler<T> th = (TypeHandler<T>) Jdbcs.getDB2BeanMappingType(targetClass, columnType);
        return th.get(rs, 1);
    }

}
