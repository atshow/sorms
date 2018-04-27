package sf.database.jdbc.extension;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import sf.database.dialect.DBDialect;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.ColumnMapping;
import sf.tools.StringUtils;
import sf.tools.utils.Assert;

import java.lang.reflect.Type;
import java.sql.*;

/**
 * 扩展Java-DB的映射规则，将复杂对象转换为JSON与数据库中的文本进行对应
 * @author
 */
public class ObjectJsonMapping<T> implements TypeHandler<T> {
    private SerializerFeature feature = null;
    private SerializerFeature[] features = new SerializerFeature[0];
    private Class<T> clazz = (Class<T>) Object.class;
    private ColumnMapping cm;
    private Type genericType;

    @Override
    public void setDefaultJavaType(Class<T> clz) {
        Assert.notNull(clz, "自定义类型不能为空");
        clazz = clz;
    }

    @Override
    public Class<T> getDefaultJavaType() {
        return clazz;
    }

    @Override
    public int getSqlType() {
        return Types.VARCHAR;
    }

    @Override
    public void setColumnMapping(ColumnMapping columnMapping) {
        this.cm = columnMapping;
        this.genericType = cm.getFieldAccessor().getGenericType();
        this.clazz = (Class<T>) cm.getClz();
    }

    @Override
    public T get(ResultSet rs, int n) throws SQLException {
        String o = rs.getString(n);
        if (StringUtils.isEmpty(o))
            return null;
        if (genericType != null) {
            return JSON.parseObject(o, genericType);
        } else {
            return JSON.parseObject(o, clazz);
        }
    }


    @Override
    public T get(ResultSet rs, String columnName) throws SQLException {
        String o = rs.getString(columnName);
        if (StringUtils.isEmpty(o))
            return null;
        if (genericType != null) {
            return JSON.parseObject(o, genericType);
        } else {
            return JSON.parseObject(o, clazz);
        }
    }

    @Override
    public T get(CallableStatement cs, int index) throws SQLException {
        String o = cs.getString(index);
        if (StringUtils.isEmpty(o))
            return null;
        if (genericType != null) {
            return JSON.parseObject(o, genericType);
        } else {
            return JSON.parseObject(o, clazz);
        }
    }


    @Override
    public T get(CallableStatement cs, String parameterName) throws SQLException {
        String o = cs.getString(parameterName);
        if (StringUtils.isEmpty(o))
            return null;
        if (genericType != null) {
            return JSON.parseObject(o, genericType);
        } else {
            return JSON.parseObject(o, clazz);
        }
    }

    @Override
    public void update(ResultSet rs, String columnLabel, Object value) throws SQLException {
        String s = JSON.toJSONString(value, features);
        rs.updateString(columnLabel, s);
    }

    @Override
    public void update(ResultSet rs, int columnIndex, Object value) throws SQLException {
        String s = JSON.toJSONString(value, features);
        rs.updateString(columnIndex, s);
    }

    @Override
    public Object set(PreparedStatement ps, Object obj, int index) throws SQLException {
        if (obj == null) {
            ps.setNull(index, getSqlType());
        } else {
            String s = JSON.toJSONString(obj, features);
            ps.setString(index, s);
        }
        return null;
    }

    @Override
    public String getSqlExpression(Object value, DBDialect profile) {
        String s = JSON.toJSONString(value);
        return wrapSqlStr(s);
    }

    /**
     * 用单引号包围字符串，并将其中的单引号按SQL转义
     * @param s
     * @return
     */
    public final static String wrapSqlStr(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        sb.append('\'');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                sb.append("''");
            } else {
                sb.append(c);
            }
        }
        sb.append('\'');
        return sb.toString();
    }

    public SerializerFeature getFeature() {
        return feature;
    }

    public void setFeature(SerializerFeature feature) {
        this.feature = feature;
        this.features = new SerializerFeature[]{feature};
    }
}
