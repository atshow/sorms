package sf.querydsl;

import com.querydsl.core.JoinExpression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.*;
import com.querydsl.sql.types.Null;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.OrmContext;
import sf.database.OrmParameter;
import sf.database.jdbc.extension.ObjectJsonMapping;
import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;
import sf.tools.utils.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueryDSL {

    private static Field configurationQuery;
    private static Field configurationDML;

    private static Method serialize;

    static {
        if (serialize == null) {
            try {
                serialize = ProjectableSQLQuery.class.getDeclaredMethod("serialize", Boolean.TYPE);
                serialize.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Map<Class, SQLRelationalPath> sqlRelationalPathBaseMap = new ConcurrentHashMap<>();


    public static JPAEntityPath entityPathBase(Class<? extends DBObject> clz) {
        JPAEntityPath pb = new JPAEntityPath(clz);
        return pb;
    }

    public static void initRelationalPathBase(Map<Class<?>, TableMapping> classMap) {
        for (Map.Entry<Class<?>, TableMapping> entry : classMap.entrySet()) {
            SQLRelationalPath pb = new SQLRelationalPath(entry.getKey());
            sqlRelationalPathBaseMap.put(entry.getKey(), pb);
        }
    }

    public static <T extends DBObject> SQLRelationalPath<T> relationalPathBase(Class<T> clz) {
        TableMapping tm = MetaHolder.getMeta(clz);
        SQLRelationalPath pb = relationalPathBase(tm);
        return pb;
    }

    public static <T extends DBObject> SQLRelationalPath<T> relationalPathBase(TableMapping tm) {
        SQLRelationalPath pb = tm.getRelationalPath();
        if (pb == null) {
            pb = sqlRelationalPathBaseMap.get(tm.getThisType());
        }
        if (pb == null) {
            pb = new SQLRelationalPath(tm.getThisType());
            sqlRelationalPathBaseMap.put(tm.getThisType(), pb);
            tm.setRelationalPath(pb);
        }
        return pb;
    }

    /**
     * 获取内部的SQLRelationalPathBase
     * @param query
     * @return
     */
    public static List<Class<?>> getJoins(AbstractSQLQuery query) {
        List<JoinExpression> exprs = query.getMetadata().getJoins();
        List<Class<?>> list = Collections.emptyList();
        if (CollectionUtils.isNotEmpty(exprs)) {
            list = new ArrayList<>(exprs.size());
            for (JoinExpression je : exprs) {
                Class<?> clz = je.getTarget().getType();
                list.add(clz);
            }
        }
        return list;
    }

    /**
     * 获取表class
     * @param query
     * @return
     */
    public static Class<?> getQueryDSLTableClass(AbstractSQLQuery query) {
        List<Class<?>> joinTargets = QueryDSL.getJoins(query);
        Class<?> clz = null;
        for (Class<?> c : joinTargets) {
            if (DBObject.class.isAssignableFrom(c)) {
                clz = c;
                break;
            }
        }
        return clz;
    }

    private static Field insertEntity = null;

    public static Class<?> getInsertTableClass(AbstractSQLInsertClause intsert) {
        Class<?> clz = null;
        try {
            if (insertEntity == null) {
                insertEntity = AbstractSQLInsertClause.class.getDeclaredField("entity");
                insertEntity.setAccessible(true);
            }
            RelationalPathBase c = (RelationalPathBase) insertEntity.get(intsert);
            if (c != null && c instanceof SQLRelationalPath) {
                clz = ((SQLRelationalPath) c).getClz();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clz;
    }

    private static Field updateEntity = null;

    public static Class<?> getUpdateTableClass(AbstractSQLUpdateClause update) {
        Class<?> clz = null;
        try {
            if (updateEntity == null) {
                updateEntity = AbstractSQLUpdateClause.class.getDeclaredField("entity");
                updateEntity.setAccessible(true);
            }
            RelationalPathBase c = (RelationalPathBase) updateEntity.get(update);
            if (c != null && c instanceof SQLRelationalPath) {
                clz = ((SQLRelationalPath) c).getClz();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clz;
    }

    private static Field deleteEntity = null;

    public static Class<?> getDeleteTableClass(AbstractSQLDeleteClause delete) {
        Class<?> clz = null;
        try {
            if (deleteEntity == null) {
                deleteEntity = AbstractSQLDeleteClause.class.getDeclaredField("entity");
                deleteEntity.setAccessible(true);
            }
            RelationalPathBase c = (RelationalPathBase) deleteEntity.get(delete);
            if (c != null && c instanceof SQLRelationalPath) {
                clz = ((SQLRelationalPath) c).getClz();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clz;
    }

    private static Field mergeEntity = null;

    public static Class<?> getMergeTableClass(SQLMergeClause merge) {
        Class<?> clz = null;
        try {
            if (mergeEntity == null) {
                mergeEntity = SQLMergeClause.class.getDeclaredField("entity");
                mergeEntity.setAccessible(true);
            }
            RelationalPathBase c = (RelationalPathBase) mergeEntity.get(merge);
            if (c != null && c instanceof SQLRelationalPath) {
                clz = ((SQLRelationalPath) c).getClz();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clz;
    }


    /**
     * 设置返回自定义类型
     * @param clz
     * @param query
     * @param <T>
     */
    public static <T> void setReturnType(Class<T> clz, AbstractSQLQuery query) {
        Configuration c = getConfigurationQuery(query);
        TableMapping tm = MetaHolder.getMeta(clz);
        setReturnType(clz, c);
    }

    /**
     * @param clz
     * @param c
     * @param <T>
     */
    public static <T> void setReturnType(Class<T> clz, Configuration c) {
        TableMapping tm = MetaHolder.getMeta(clz);
        for (Map.Entry<DBField, ColumnMapping> entry : tm.getSchemaMap().entrySet()) {
            ColumnMapping cm = entry.getValue();
            //自定义json处理注入
            if (cm.getType() != null && cm.getType().value() == ObjectJsonMapping.class) {
                Class<?> javaType = c.getJavaType(0, null, 0, 0, tm.getTableName(), cm.getRawColumnName());
                if (javaType == Null.class || javaType == null) {
                    QueryDSLObjectMapJsonType<Object> queryDSLObjectMapJsonType = new QueryDSLObjectMapJsonType<>();
                    queryDSLObjectMapJsonType.setReturnedClass(cm.getHandler().getDefaultJavaType());
                    queryDSLObjectMapJsonType.setGenericType(cm.getFieldAccessor().getGenericType());
                    c.register(tm.getTableName(), cm.getRawColumnName(), queryDSLObjectMapJsonType);
                }
            }
        }
    }

    public static Configuration getConfigurationQuery(AbstractSQLQuery query) {
        try {
            if (query instanceof OrmSQLQuery) {
                return ((OrmSQLQuery) query).getConfiguration();
            }
            if (configurationQuery == null) {
                Field f = ProjectableSQLQuery.class.getDeclaredField("configuration");
                f.setAccessible(true);
                configurationQuery = f;
            }
            Configuration c = (Configuration) configurationQuery.get(query);
            return c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration getConfigurationDML(AbstractSQLClause query) {
        try {
            if (configurationDML == null) {
                Field f = AbstractSQLClause.class.getDeclaredField("configuration");
                f.setAccessible(true);
                configurationDML = f;
            }
            Configuration c = (Configuration) configurationDML.get(query);
            return c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param query
     * @param context
     * @return 代表是否是实体表操作
     */
    public static boolean getOrmContext(AbstractSQLQuery query, OrmContext context) {
        SQLBindings sqlBindings = query.getSQL();
        SQLSerializer serializer = null;
        if (query instanceof OrmSQLQuery) {
            serializer = ((OrmSQLQuery) query).serialize(false);
        } else {
            try {
                serializer = (SQLSerializer) serialize.invoke(query, false);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        context.setSql(sqlBindings.getSQL());
        List<Object> constants = sqlBindings.getNullFriendlyBindings();
        List<Path<?>> paths = serializer.getConstantPaths();

        List<OrmParameter> paras = new ArrayList<>();
        boolean staticMode = setParameters(constants, paths, paras);
        context.setParas(paras);
        return staticMode;
    }

    protected static boolean setParameters(List<?> objects, List<Path<?>> constantPaths, List<OrmParameter> paras) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() +
                    " paths, but got " + constantPaths.size());
        }
        //代表是否是实体表操作
        boolean staticModel = true;
        Iterator<Path<?>> pathIt = constantPaths.iterator();
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
            Path<?> path = pathIt.next();
            RelationalPath pb = (RelationalPath) path.getMetadata().getParent();
            if (pb instanceof SQLRelationalPath) {
                SQLRelationalPath rpb = (SQLRelationalPath) pb;
                TableMapping tm = MetaHolder.getMeta(rpb.getClz());
                String element = (String) path.getMetadata().getElement();
                ColumnMapping cm = tm.getMetaFieldMap().get(element);
                paras.add(new OrmParameter(o, cm));
            } else {
                paras.add(new OrmParameter().setValue(o));
                staticModel = false;
            }
        }
        return staticModel;
    }
}
