package sf.database.meta;

import sf.common.exception.OrmException;
import sf.database.DBCascadeField;
import sf.database.DBField;
import sf.database.DBObject;
import sf.database.annotations.*;
import sf.database.dbinfo.ColumnDBType;
import sf.database.jdbc.type.Jdbcs;
import sf.tools.ArrayUtils;
import sf.tools.StringUtils;
import sf.tools.reflect.PropertyHold;
import sf.tools.reflect.UnsafeUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.CollectionUtils;
import sf.tools.utils.ReflectionUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class MetaHolder {
    // Schema映射
    static Map<String, String> SCHEMA_MAPPING;
    // 站点映射
    static Map<String, String> SITE_MAPPING;

    // 元数据池
    static final Map<Class<?>, TableMapping> pool = new IdentityHashMap<Class<?>, TableMapping>(32);
    // 动态表元数据池
    static final Map<String, TableMapping> dynPool = new ConcurrentHashMap<String, TableMapping>(32);
    // 反向查找表
    private static final Map<String, TableMapping> inverseMapping = new ConcurrentHashMap<String, TableMapping>();

    /**
     * 数据库字段对应表
     */
    private static final Map<DBField, TableMapping> dbField2Tables = new ConcurrentHashMap<>();

    /**
     * 根据类获取表模型
     * @param clz
     * @return
     */
    public static final TableMapping getMeta(Class<?> clz) {
        Assert.notNull(clz, "");
        TableMapping m = pool.get(clz);
        if (m == null) {
            m = initData(clz);
            pool.put(clz, m);
            //暂不设置.
            //  m.setRelationalPath(new SQLRelationalPath(m));
            //处理级联
            cascade(m);
        }
        return m;
    }

    /**
     * 在获取类时，需要有一个标记快速判断该类是否经过增强（无论是动态增强还是静态增强）一旦发现没增强的类，就抛出异常�?
     * @param clz
     * @return
     */
    private static TableMapping initData(Class<?> clz) {
        {
            TableMapping m1 = pool.get(clz);
            if (m1 != null)
                return m1; // 双重检查锁定
        }
        TableMapping tm = initPojo(clz);
        return tm;

    }

    private static TableMapping initPojo(Class<?> clz) {
        TableMapping meta = new TableMapping();
        meta.setThisType(clz);

        Entity entity = clz.getAnnotation(Entity.class);
        Table table = clz.getAnnotation(Table.class);
        Comment tableComment = clz.getAnnotation(Comment.class);
        BindDataSource bindDataSource = clz.getAnnotation(BindDataSource.class);
        meta.setComment(tableComment);
        List<Field> fields = new ArrayList(Arrays.asList(clz.getDeclaredFields()));
        Method[] methods = clz.getDeclaredMethods();
        Class<?>[] subcls = clz.getDeclaredClasses();

        if (bindDataSource != null) {
            meta.setBindDsName(bindDataSource.value());
        }

        //提取父类字段
        Class<?> clazz = clz.getSuperclass();
        for (; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }

        if (entity != null) {
            meta.setTable(table);
            if (table != null) {
                meta.setSchema(table.schema());
                meta.setTableName(table.name() != null ? table.name() : clz.getName());
                meta.setCatalog(table.catalog());
                if (table.indexes().length > 0) {
                    meta.setIndexs(Arrays.asList(table.indexes()));
                }
                if (table.uniqueConstraints().length > 0) {
                    meta.setUniques(Arrays.asList(table.uniqueConstraints()));
                }
            } else {
                meta.setTableName(clz.getName());
            }
        }

        List<String> enumFields = new ArrayList<>();
        List<String> enumCascadeFields = new ArrayList<>();
        // 解析数据库对应的枚举类
        if (subcls != null && subcls.length > 0) {
            for (Class<?> cls : subcls) {
                if (/* Modifier.isStatic(cls.getModifiers()) && */cls.isEnum() && DBField.class.isAssignableFrom(cls)) {
                    Class<? extends Enum> sub = cls.asSubclass(Enum.class);
                    for (Enum<?> f : sub.getEnumConstants()) {
                        DBField field = (DBField) f;
                        enumFields.add(field.name());// 父类的放在后面，子类的放在前面。
                        meta.getFields().put(field.name(), field);
                        meta.getLowerFields().put(field.name().toLowerCase(), field);
                    }
                    continue;
                } else if (/* Modifier.isStatic(cls.getModifiers()) && */cls.isEnum() && DBCascadeField.class.isAssignableFrom(cls)) {
                    Class<? extends Enum> sub = cls.asSubclass(Enum.class);
                    for (Enum<?> f : sub.getEnumConstants()) {
                        DBCascadeField field = (DBCascadeField) f;
                        enumCascadeFields.add(field.name());
                        enumFields.add(field.name());// 父类的放在后面，子类的放在前面。
                        meta.getCascadeFields().put(field.name(), field);
                    }
                    continue;
                }
            }
        }
        // 解析字段 不支持方法上加注解
        if (CollectionUtils.isNotEmpty(fields)) {
            for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
                Field f = it.next();
                if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                    it.remove();
                }
            }
            for (Field f : fields) {
                Id id = f.getAnnotation(Id.class);
                GeneratedValue gv = f.getAnnotation(GeneratedValue.class);
                Column col = f.getAnnotation(Column.class);
                Temporal temporal = f.getAnnotation(Temporal.class);
                Lob lob = f.getAnnotation(Lob.class);
                Transient trans = f.getAnnotation(Transient.class);
                ManyToMany mtm = f.getAnnotation(ManyToMany.class);
                ManyToOne mto = f.getAnnotation(ManyToOne.class);
                OneToMany otm = f.getAnnotation(OneToMany.class);
                OneToOne oto = f.getAnnotation(OneToOne.class);
                JoinColumn jc = f.getAnnotation(JoinColumn.class);
                JoinColumns jcs = f.getAnnotation(JoinColumns.class);
                JoinTable jt = f.getAnnotation(JoinTable.class);
                Comment comment = f.getAnnotation(Comment.class);
                SequenceGenerator sequenceGenerator = f.getAnnotation(SequenceGenerator.class);
                TableGenerator tableGenerator = f.getAnnotation(TableGenerator.class);
                OrderBy orderBy = f.getAnnotation(OrderBy.class);
                Version version = f.getAnnotation(Version.class);

                //自定义注解(非JAP标准)
                Type type = f.getAnnotation(Type.class);
                FetchDBField fetchDBField = f.getAnnotation(FetchDBField.class);
                UniqueKeyGenerator keyGenerator = f.getAnnotation(UniqueKeyGenerator.class);

                // if (enumFields.contains(f.getName())) {
                ColumnMapping cm = new ColumnMapping();
                cm.setMeta(meta);
                cm.setFieldName(f.getName());
                cm.setClz(f.getType());
                cm.setComment(comment);
                cm.setJpaTransient(trans);
                cm.setFetchDBField(fetchDBField);
                cm.setVersion(version != null);

                cm.setUniqueKeyGenerator(keyGenerator);
                cm.setSequenceGenerator(sequenceGenerator);
                cm.setOrderBy(orderBy);

                if (cm.getUniqueKeyGenerator() != null && cm.getUniqueKeyGenerator().targetClass() != null) {
                    try {
                        cm.setIdentifierGenerator(cm.getUniqueKeyGenerator().targetClass().newInstance());
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (mtm != null) {
                    cm.setManyToMany(mtm);
                }
                if (mto != null) {
                    cm.setManyToOne(mto);
                }
                if (otm != null) {
                    cm.setOneToMany(otm);
                }
                if (oto != null) {
                    cm.setOneToOne(oto);
                    if (meta.getAllOne2One() == null) {
                        meta.setAllOne2One(new ArrayList<>());
                    }
                    meta.getAllOne2One().add(cm);
                }
                if (jc != null) {
                    cm.setJoinColumn(jc);
                }
                if (jcs != null) {
                    cm.setJoinColumns(jcs);
                }
                if (jt != null) {
                    cm.setJoinTable(jt);
                }

                if (gv != null) {
                    cm.setGv(gv);
                }
                if (type != null) {
                    cm.setType(type);
                }
                if (id != null) {
                    cm.setPk(true);
                    if (meta.getPkFields() == null) {
                        meta.setPkFields(new ArrayList<ColumnMapping>());
                    }
                    meta.getPkFields().add(cm);
                }
                if (col != null) {
                    cm.setColumn(col);
                    cm.setRawColumnName(col.name());
                    if (StringUtils.isBlank(col.name())) {
                        cm.setRawColumnName(f.getName());
                    }
                    cm.setUpperColumnName(cm.getRawColumnName().toUpperCase());
                    cm.setLowerColumnName(cm.getRawColumnName().toLowerCase());
                    meta.getFieldToColumn().put(meta.getFields().get(f.getName()),
                            col.name() == null ? f.getName() : col.name());
                    meta.getLowerColumnToFieldName().put(col.name() == null ? f.getName() : col.name(),
                            f.getName());
                } else {
                    cm.setRawColumnName(f.getName());
                    cm.setUpperColumnName(cm.getRawColumnName().toUpperCase());
                    cm.setLowerColumnName(cm.getRawColumnName().toLowerCase());
                    meta.getFieldToColumn().put(meta.getFields().get(f.getName()), f.getName());
                    meta.getLowerColumnToFieldName().put(f.getName(), f.getName());
                }
                if (lob != null) {
                    cm.setLob(true);
                    List<DBField> lobs = meta.getLobNames();
                    if (lobs == null) {
                        lobs = new ArrayList<DBField>();
                        meta.setLobNames(lobs);
                        lobs.add(meta.getFields().get(f.getName()));
                    }
                }
                if (temporal != null) {
                    cm.setTemporal(temporal);
                }
                // 设置泛型
                PropertyHold ph = new PropertyHold();
                ph.setField(f);
                ph.setName(f.getName());// -- 设置泛型原始类型
                ph.setFieldOffset(UnsafeUtils.getUnsafe().objectFieldOffset(f));
                if (Collection.class.isAssignableFrom(cm.getClz()) || Map.class.isAssignableFrom(cm.getClz())) {
                    java.lang.reflect.Type[] types = ReflectionUtils.getParameterizedType(f);
                    if (types != null && types.length > 0) {
                        Class[] classes = new Class[types.length];
                        for (int i = 0; i < types.length; i++) {
                            classes[i] = (Class) types[i];
                        }
                        ph.setRawTypes(classes);
                    }
                }
                cm.setFieldAccessor(ph);
                //设置DBField
                cm.setField(meta.getFields().get(f.getName()));
                cm.setCascadeField(meta.getCascadeFields().get(f.getName()));

                cm.setHandler(Jdbcs.getBean2DBMappingType(cm));
                cm.setSqlType(cm.getHandler().getSqlType());
                cm.setColumnDef(new ColumnDBType(cm));
                meta.getMetaFields().add(cm);
                meta.getMetaFieldMap().put(cm.getRawColumnName(), cm);
                DBField field = meta.getFields().get(f.getName());
                if (field != null) {
                    meta.getSchemaMap().put(field, cm);
                    dbField2Tables.put(field, meta);
                    if (cm.isVersion()) {
                        meta.getVersionMap().put(field, cm);
                    }
                }
                // }

            }
        }
        // 不支持从方法写注解
        if (ArrayUtils.isNotEmpty(methods)) {
            for (Method m : methods) {
                String methodName = m.getName();
                String fieldName = "";
                if (methodName.startsWith("get") || methodName.startsWith("set")) {
                    fieldName = methodName.substring(3, methodName.length());
                } else if (methodName.startsWith("is")) {
                    fieldName = methodName.substring(2, methodName.length());
                } else {
                    continue;
                }
                fieldName = StringUtils.swapCase(String.valueOf(fieldName.charAt(0))) + fieldName.substring(1);
                for (ColumnMapping cm : meta.getMetaFields()) {
                    if (cm.getFieldName().equals(fieldName)) {
                        if (methodName.startsWith("get") || methodName.startsWith("is")) {
                            cm.getFieldAccessor().setGetter(m);
                            break;
                        } else if (methodName.startsWith("set")) {
                            cm.getFieldAccessor().setSetter(m);
                            break;
                        }
                    }
                }
            }
        }
        //校验字段是否存在
        checkDBField(meta);
        //检查级联字段
        checkDBCascadeField(meta);
        return meta;
    }

    /**
     * 校验字段是否存在
     * @param meta
     */
    private static void checkDBField(TableMapping meta) {
        //从列中查找枚举
        for (ColumnMapping cm : meta.getMetaFields()) {
            if (cm.getColumn() != null || cm.isPk() || cm.getTemporal() != null || cm.getEnumerated() != null || cm.isVersion()) {
                boolean exist = true;
                if (meta.getFields().get(cm.getFieldName()) == null) {
                    exist = false;
                }
                if (!exist) {
                    throw new OrmException("字段[" + cm.getFieldName() + "--->>" + cm.getMeta().getThisType() + "]在类中中已定义,但未找到该字段的枚举定义!");
                }
            }
        }
        //从枚举中查找列
        for (Entry<String, DBField> entry : meta.getFields().entrySet()) {
            ColumnMapping cm = meta.getSchemaMap().get(entry.getValue());
            if (cm == null) {
                throw new OrmException("字段[" + entry.getValue().name() + "--->>" + entry.getValue().getClass() + "]在枚举中已定义,但未找到该字段!");
            }
        }
    }

    /**
     * 校验级联字段是否存在
     * @param meta
     */
    private static void checkDBCascadeField(TableMapping meta) {
        //从列中查找枚举
        for (ColumnMapping cm : meta.getMetaFields()) {
            if (cm.isCascade()) {
                boolean exist = false;
                for (Entry<String, DBCascadeField> entry : meta.getCascadeFields().entrySet()) {
                    if (Objects.equals(entry.getValue(), cm.getCascadeField())) {
                        exist = true;
                    }
                }
                if (!exist) {
                    throw new OrmException("字段[" + cm.getFieldName() + "--->>" + cm.getMeta().getThisType() + "]在类中中已定义,但未找到该字段的级联定义!");
                }
            }
        }
        //从枚举中查找列
        for (Entry<String, DBCascadeField> entry : meta.getCascadeFields().entrySet()) {
            boolean f = false;
            for (ColumnMapping cm : meta.getMetaFields()) {
                if (cm.isCascade() && Objects.equals(cm.getCascadeField(), entry.getValue())) {
                    f = true;
                }
            }
            if (!f) {
                throw new OrmException("字段[" + entry.getKey() + "--->>" + meta.getThisType() + "]在类中中已定义,但未找到该字段的级联定义!");
            }
        }
    }

    /**
     * 处理级联
     */
    public static void cascade(TableMapping tm) {
        for (ColumnMapping cm : tm.getMetaFields()) {
            if (cm.getManyToMany() != null || cm.getOneToMany() != null || cm.getManyToOne() != null || cm.getOneToOne() != null) {
                cascade(tm, cm);
            }
        }
    }

    public static void cascade(TableMapping tm, ColumnMapping cm) {
        CascadeConfig cc = CascadeUtils.doCascade((Class<? extends DBObject>) tm.getThisType(), cm);
        cm.setCascadeConfig(cc);
    }


    /**
     * 逆向查找元模型
     * @param schema
     * @param table
     */
    public static TableMapping lookup(String schema, String table) {
        String key = (schema + "." + table).toUpperCase();
        TableMapping m = inverseMapping.get(key);
        if (m != null)
            return m;

        // Schema还原
        if (schema != null) {
            schema = schema.toUpperCase();
            for (Entry<String, String> e : SCHEMA_MAPPING.entrySet()) {
                if (e.getValue().equals(schema)) {
                    schema = e.getKey();
                    break;
                }
            }
        }

        // Lookup static models
        for (TableMapping meta : pool.values()) {
            String tablename = meta.getTableName();
            if (schema != null && (!StringUtils.equals(meta.getSchema(), schema))) {// schema不同则跳
                continue;
            }
            if (tablename.equalsIgnoreCase(table)) {
                m = meta;
                break;
            }
        }
        if (m == null) {
            // Lookup dynamic models
            for (TableMapping meta : dynPool.values()) {
                String tablename = meta.getTableName();
                if (schema != null && (!StringUtils.equals(meta.getSchema(), schema))) {// schema不同则跳
                    continue;
                }
                if (tablename.equalsIgnoreCase(table)) {
                    m = meta;
                    break;
                }
            }
        }
        inverseMapping.put(key, m);
        return m;
    }

    /**
     * @param f
     * @return
     */
    public static TableMapping getTableMapping(DBField f, String clzName) {
        TableMapping tm = dbField2Tables.get(f);
        if (tm == null) {
            try {
                Class<?> clz = Class.forName(clzName);
                MetaHolder.initData(clz);
                tm = dbField2Tables.get(f);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return tm;
    }

    public static TableMapping getTableMapping(DBField field) {
        Class<? extends DBField> clz = field.getClass();
        String name = clz.getName().substring(0, clz.getName().lastIndexOf("$"));
        TableMapping tm = MetaHolder.getTableMapping(field, name);
        return tm;
    }

    public static Map<Class<?>, TableMapping> getAllClass() {
        return Collections.unmodifiableMap(pool);
    }

    public static void clear() {
        pool.clear();
        dynPool.clear();
        inverseMapping.clear();
    }
}
