package sf.database.jdbc.sql;

import sf.common.exception.OrmException;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.OrmContext;
import sf.database.OrmParameter;
import sf.database.dbinfo.Feature;
import sf.database.dialect.DBDialect;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.database.template.sql.SQLParameter;
import sf.database.util.OrmValueUtils;
import sf.tools.JavaTypeUtils;
import sf.tools.NumberUtils;
import sf.tools.StringUtils;

import javax.persistence.GenerationType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommonSql {

    public static void close(AutoCloseable obj) {
        try {
            obj.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void fillListStatement(PreparedStatement pst, List<Object> paras) throws SQLException {
        int i = 1;
        for (Object val : paras) {
            if (val != null) {
                if (val.getClass() == java.util.Date.class) {
                    val = new Timestamp(((java.util.Date) val).getTime());
                }
                pst.setObject(i, val);
            } else {
                pst.setNull(i, Types.VARCHAR);
            }
            i++;
        }
    }

    public static void fillArrayStatement(PreparedStatement pst, Object... paras) throws SQLException {
        int i = 1;
        for (Object val : paras) {
            if (val != null) {
                if (val.getClass() == java.util.Date.class) {
                    val = new Timestamp(((java.util.Date) val).getTime());
                }
                pst.setObject(i, val);
            } else {
                pst.setNull(i, Types.VARCHAR);
            }
            i++;
        }
    }

    public static void fillOrmStatement(PreparedStatement pst, List<OrmParameter> paras) throws SQLException {
        int i = 1;
        for (OrmParameter p : paras) {
            p.getColumnMapping().getHandler().set(pst, p.getValue(), i++);
        }
    }

    public static void fillSQLStatement(PreparedStatement pst, List<SQLParameter> paras) throws SQLException {
        int i = 1;
        for (SQLParameter p : paras) {
            if (p.getHandler() != null) {
                p.getHandler().set(pst, p.getValue(), i);
            } else if (p.getJdbcType() == 0) {
                if (p.getValue() == null) {
                    pst.setNull(i, Types.VARCHAR);
                } else {
                    pst.setObject(i, p.getValue());
                }
            } else {
                if (p.getValue() == null) {
                    pst.setNull(i, p.getJdbcType());
                } else {
                    pst.setObject(i, p.getJdbcType());
                }
            }
            i++;
        }
    }

    /**
     * @param table
     * @param dialect
     * @param obj
     * @param useOptimisticLock 是否使用乐观锁
     * @return
     */
    public static OrmContext model2Save(TableMapping table, DBDialect dialect, DBObject obj, boolean useOptimisticLock) {
        OrmContext c = new OrmContext(table);
        StringBuilder sql = new StringBuilder();
        List<OrmParameter> paras = new ArrayList<>();
        sql.append("insert into ").append(table.getTableName()).append("(");
        List<String> cStr = new ArrayList<>();// 字段列表
        List<String> vStr = new ArrayList<>();// 值列表
        //sequence支持
        for (ColumnMapping cm : table.getMetaFields()) {
            if (cm.getGv() != null && cm.getGv().strategy() == GenerationType.SEQUENCE && StringUtils.isNotBlank(cm.getGv().generator())) {
                if (!dialect.has(Feature.SUPPORT_SEQUENCE)) {
                    throw new RuntimeException("数据库不支持sequence");
                }
                boolean exist = false;
                for (Map.Entry<DBField, Object> e : obj.updateValueMap().entrySet()) {
                    if (cm.getField() == e.getKey()) {
                        exist = true;
                        break;
                    }
                }
                //不存在,则添加
                if (!exist) {
                    cStr.add(cm.getRawColumnName());
                    vStr.add(dialect.getSeqNextValSql(cm.getGv().generator()));
                }
            } else if (cm.getUniqueKeyGenerator() != null) {
                //处理自定义主键的场景
                cStr.add(cm.getRawColumnName());
                vStr.add("?");
                paras.add(new OrmParameter(null, cm));
            }
        }
        //处理值
        for (Map.Entry<DBField, Object> e : obj.updateValueMap().entrySet()) {
            DBField colName = e.getKey();
            ColumnMapping column = table.getSchemaMap().get(colName);
            if (column != null) {
                cStr.add(column.getRawColumnName());
                vStr.add("?");
                paras.add(new OrmParameter(e.getValue(), column));
            }
        }
        if (useOptimisticLock) {
            //乐观锁插入处理
            optimisticLockInsert(obj, paras, cStr, vStr);
        }
        sql.append(StringUtils.join(cStr, ",")).append(") values(").append(StringUtils.join(vStr, ",")).append(")");
        c.setSql(sql.toString());
        c.setParas(paras);
        return c;
    }

    /**
     * 乐观锁插入处理
     * @param obj
     * @param paras
     * @param cStr
     * @param vStr
     */
    private static void optimisticLockInsert(DBObject obj, List<OrmParameter> paras, List<String> cStr, List<String> vStr) {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        if (!table.getVersionMap().isEmpty()) {
            for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                ColumnMapping cm = e.getValue();
                String columnName = cm.getRawColumnName();
                Object value = obj.updateValueMap().get(e.getKey());
                if (value == null) {
                    if (JavaTypeUtils.isNumberClass(cm.getClz())) {
                        //数字,则版本加1
                        value = 0;
                    } else if (cm.getClz() == String.class) {
                        //字符串,则使用UUID
                        value = StringUtils.uuid32();
                    } else if (Date.class.isAssignableFrom(cm.getClz())) {
                        //日期 则使用当前日期
                        value = new Date();
                    } else {
                        throw new UnsupportedOperationException("乐观锁:" + e.getKey().getClass() + " 设置有误");
                    }
                    //回写值
                    OrmValueUtils.setValue(obj, cm, value);
                }
                if (value != null) {
                    cStr.add(columnName);
                    vStr.add("?");
                    paras.add(new OrmParameter(value, cm));
                }
            }
        }
    }

    /**
     * 更新对象,如果有乐观锁,会根据乐观锁条件更新,并会更新数据库中的乐观锁条件.但不会更新对象的乐观锁字段的值.
     * @param table
     * @param obj
     * @param useOptimisticLock 是否使用乐观锁
     * @return
     */
    public static OrmContext forModelUpdate(TableMapping table, DBObject obj, boolean useOptimisticLock) {
        OrmContext c = new OrmContext(table);
        StringBuilder sql = new StringBuilder();
        List<OrmParameter> paras = new ArrayList<>();
        List<ColumnMapping> pKeys = table.getPkFields();
        //检查乐观锁前置条件
        if (useOptimisticLock) {
            if (!table.getVersionMap().isEmpty()) {
                for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                    if (!obj.updateValueMap().containsKey(e.getKey())) {
                        throw new OrmException("乐观锁未设置:" + e.getKey().getClass());
                    }
                }
            }
        }
        sql.append("update ").append(table.getTableName()).append(" set ");
        List<String> setStr = new ArrayList<>();
        for (Map.Entry<DBField, Object> e : obj.updateValueMap().entrySet()) {
            DBField dbField = e.getKey();
            ColumnMapping cm = table.getSchemaMap().get(dbField);
            //判断是否是主键
            boolean isPk = cm.isPk();


            //判断是否是乐观锁,如果是,则忽略.
            boolean version = false;
            if (useOptimisticLock) {
                version = cm.isVersion();
            }
            //必须包含该字段,且不为主键,不是乐观锁字段.
            if (table.getFieldToColumn().containsKey(dbField) && !isPk && !version) {
                setStr.add(cm.getRawColumnName() + " =? ");
                paras.add(new OrmParameter(e.getValue(), cm));
            }
        }
        if (useOptimisticLock) {
            //乐观锁处理
            //FIXME 未考虑返回值的情况.考虑到异常情况下的重试,对更新方法不设置对象为更新后的乐观锁比较好.此处当异常时,可以重试.
            if (!table.getVersionMap().isEmpty()) {
                //记录变更后的值
                List<OrmParameter> preResultPara = new ArrayList<>();
                for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                    ColumnMapping cm = e.getValue();
                    String columnName = cm.getRawColumnName();
                    Object value = obj.updateValueMap().get(e.getKey());
                    if (JavaTypeUtils.isNumberClass(cm.getClz())) {
                        //数字,则版本加1
                        setStr.add(columnName + "=" + columnName + "+1");
                        Object preValue = ((Number) value).longValue() + 1;
                        OrmParameter op = new OrmParameter(NumberUtils.getTargetNumber((Number) preValue, cm.getClz()), cm);
                        preResultPara.add(op);
                    } else if (cm.getClz() == String.class) {
                        //字符串,则使用UUID
                        String uuid = StringUtils.uuid32();
                        setStr.add(columnName + "='" + uuid + "'");
                        OrmParameter op = new OrmParameter(uuid, cm);
                        preResultPara.add(op);
                    } else if (Date.class.isAssignableFrom(cm.getClz())) {
                        //日期 则使用当前日期
                        setStr.add(columnName + "=?");
                        OrmParameter op = new OrmParameter(new Date(), cm);
                        paras.add(op);
                        preResultPara.add(op);
                    }
                }
                //设置处理后的返回值
                c.setPreResultParas(preResultPara);
            }
        }
        sql.append(StringUtils.join(setStr, ","));
        sql.append(" where ");
        List<String> cStr = new ArrayList<>();//条件
        for (int i = 0; i < pKeys.size(); i++) {
            ColumnMapping pk = pKeys.get(i);
            cStr.add(pk.getRawColumnName() + " = ?");
            Object o = OrmValueUtils.getValue(obj, pKeys.get(i));
            paras.add(new OrmParameter(o, pk));
        }
        if (useOptimisticLock) {
            //乐观锁设置where值
            if (!table.getVersionMap().isEmpty()) {
                for (Map.Entry<DBField, ColumnMapping> e : table.getVersionMap().entrySet()) {
                    ColumnMapping cm = e.getValue();
                    Object value = obj.updateValueMap().get(e.getKey());
                    String columnName = cm.getRawColumnName();
                    cStr.add(columnName + " = ?");
                    paras.add(new OrmParameter(value, cm));
                }
            }
        }
        sql.append(StringUtils.join(cStr, " and "));
        c.setParas(paras);
        c.setSql(sql.toString());
        return c;
    }

    public static OrmContext forModelDeleteById(TableMapping table, DBObject obj) {
        OrmContext c = new OrmContext(table);
        List<OrmParameter> paras = new ArrayList<>();
        List<ColumnMapping> pKeys = table.getPkFields();
        StringBuilder sql = new StringBuilder("delete from ").append(table.getTableName()).append(" where ");
        Map<DBField, Object> valueMap = obj.updateValueMap();
        boolean existPK = true;
        for (ColumnMapping cm : pKeys) {
            if (valueMap.get(cm.getField()) == null) {
                existPK = false;
            }
        }
        if (existPK || valueMap.isEmpty()) {
            for (int i = 0; i < pKeys.size(); i++) {
                if (i > 0) {
                    sql.append(" and ");
                }
                sql.append(pKeys.get(i).getRawColumnName()).append(" = ?");
                Object o = OrmValueUtils.getValue(obj, pKeys.get(i));
                paras.add(new OrmParameter().setValue(o).setColumnMapping(pKeys.get(i)));
            }
        } else {
            int i = 0;
            for (Map.Entry<DBField, Object> entry : valueMap.entrySet()) {
                if (i > 0) {
                    sql.append(" and ");
                }
                sql.append(table.getSchemaMap().get(entry.getKey()).getRawColumnName()).append(" = ?");
                Object o = entry.getValue();
                paras.add(new OrmParameter().setValue(o).setColumnMapping(table.getSchemaMap().get(entry.getKey())));
                i++;
            }
        }
        c.setSql(sql.toString());
        c.setParas(paras);
        return c;
    }

    public static OrmContext forModelSelectByIds(TableMapping table, Object... keyParams) {
        OrmContext c = new OrmContext(table);
        List<ColumnMapping> pKeys = table.getPkFields();
        List<OrmParameter> parameters = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(table.getTableName()).append(" where ");
        for (int i = 0; i < pKeys.size(); i++) {
            if (i > 0) {
                sql.append(" and ");
            }
            sql.append(pKeys.get(i).getRawColumnName()).append(" =? ");
            parameters.add(new OrmParameter(keyParams[i], pKeys.get(i)));
        }
        c.setSql(sql.toString());
        c.setParas(parameters);
        return c;
    }
}
