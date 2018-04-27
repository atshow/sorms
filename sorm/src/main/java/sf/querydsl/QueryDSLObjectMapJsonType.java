package sf.querydsl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.querydsl.sql.types.AbstractType;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * 提供对Map关系的映射
 */
public class QueryDSLObjectMapJsonType<T> extends AbstractType<T> {
    private SerializerFeature feature = null;
    private SerializerFeature[] features = new SerializerFeature[0];
    private Class<T> clazz = (Class<T>) Object.class;
    private Type genericType;

    public QueryDSLObjectMapJsonType() {
        super(Types.VARCHAR);
    }

    public QueryDSLObjectMapJsonType(int type) {
        super(type);
    }

    @Override
    public T getValue(ResultSet rs, int startIndex) throws SQLException {
        String o = rs.getString(startIndex);
        if (genericType != null) {
            return JSON.parseObject(o, genericType);
        } else {
            return JSON.parseObject(o, clazz);
        }
    }

    @Override
    public Class<T> getReturnedClass() {
        return clazz;
    }

    public void setReturnedClass(Class<T> clz) {
        this.clazz = clz;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, T value)
            throws SQLException {
        if (value == null) {
            st.setNull(startIndex, getSQLTypes()[0]);
        } else {
            String s = JSON.toJSONString(value, features);
            st.setString(startIndex, s);
        }
    }

    public Type getGenericType() {
        return genericType;
    }

    public void setGenericType(Type genericType) {
        this.genericType = genericType;
    }
}
