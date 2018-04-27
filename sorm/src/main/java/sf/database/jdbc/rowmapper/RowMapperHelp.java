package sf.database.jdbc.rowmapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: sxf
 * @Date: 2018/3/27
 */
public class RowMapperHelp {

    /**
     * 获取转为rowmapper
     * @param beanClass
     * @param <T>
     * @return
     */
    public static <T> RowMapper<T> getRowMapper(Class<T> beanClass) {
        RowMapper<T> rowMapper;
        if (beanClass.isArray()) {
            rowMapper = new ArrayRowMapper();
        } else if (Map.class.isAssignableFrom(beanClass)) {
            rowMapper = new MapRowMapper();
        } else if (Set.class.isAssignableFrom(beanClass)) {
            rowMapper = new SetRowMapper();
        } else if (List.class.isAssignableFrom(beanClass) || Collection.class.isAssignableFrom(beanClass)) {
            rowMapper = new ListRowMapper();
        } else if (beanClass.getName().startsWith("java.")) {
            rowMapper = new SingleColumnRowMapper<T>(beanClass);
        } else {
            rowMapper = new BeanRowMapper<T>(beanClass);
        }
        return rowMapper;
    }
}
