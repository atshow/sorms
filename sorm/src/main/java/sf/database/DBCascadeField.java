package sf.database;

import java.io.Serializable;

/**
 * 数据级联字段接口,对应关系表
 * @Author: sxf
 * @Date: 2018/3/25 12:24
 */
public interface DBCascadeField extends Serializable {
    String name();
}