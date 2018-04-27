package sf.database.jdbc.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 将 一行 ResultSet 转换成 T，不允许调用 rs.next()
 */
public interface RowMapper<T> {

    /**
     * @param rs     结果集
     * @param rowNum 处理的记录位置(第几条记录)：可以只针对某一条记录做特殊处理,从0开始
     * @return
     * @throws SQLException
     */
    T handle(ResultSet rs, int rowNum) throws SQLException;

}
