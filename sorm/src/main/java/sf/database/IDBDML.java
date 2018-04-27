package sf.database;

import java.sql.SQLException;
import java.util.Map;

public interface IDBDML {

    /**
     * 清除待更新数据
     */
    void clearUpdate();

    /**
     * 将UpdateValueMap中的值更新到实体字段中取（如果不相等）同时清除掉updateValueMap中的值
     */
    void applyUpdate();

    /**
     * 获取目前的updateMap
     */
    Map<DBField, Object> updateValueMap();

    /**
     * 准备更新数据
     * @param field
     * @param newValue
     * @throws SQLException
     */
    void prepareUpdate(DBField field, Object newValue);

    /**
     * 判断是否需要更新
     * @return
     */
    boolean needUpdate();
}
