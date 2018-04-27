package sf.database.meta;

import sf.database.DBField;
import sf.database.DBObject;
import sf.database.OrmContext;
import sf.database.OrmParameter;
import sf.database.annotations.FetchDBField;
import sf.database.util.OrmValueUtils;
import sf.tools.StringUtils;
import sf.tools.utils.ReflectionUtils;

import javax.persistence.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 级联关系解析为 CascadeConfig
 */
public class CascadeUtils {

    /**
     * cascade属性： 指定级联操作的行为(可多选)<br>
     * CascadeType.PERSIST 级联新增（又称级联保存）<br>
     * CascadeType.MERGE 级联合并（又称级联更新）<br>
     * CascadeType.REMOVE 级联删除<br>
     * CascadeType.REFRESH 级联刷新<br>
     * CascadeType.ALL  包含所有持久化方法
     * <p>
     * mappedBy：
     * 1.只有OneToOne，OneToMany，ManyToMany上才有mappedBy属性，ManyToOne不存在该属性；<br>
     * 2.mappedBy标签一定是定义在被拥有方的，他指向拥有方；<br>
     * 3.mappedBy的含义，应该理解为，拥有方能够自动维护跟被拥有方的关系，当然，如果从被拥有方，通过手工强行来维护拥有方的关系也是可以做到的；<br>
     * 4.mappedBy跟joinColumn/JoinTable总是处于互斥的一方，可以理解为正是由于拥有方的关联被拥有方的字段存在，拥有方才拥有了被拥有方。<br>
     * mappedBy这方定义JoinColumn/JoinTable总是失效的，不会建立对应的字段或者表。
     * @param clz
     * @param cm
     * @return
     */
    public static CascadeConfig doCascade(Class<? extends DBObject> clz, ColumnMapping cm) {
        String mappedBy = null;//被维护方
        if (cm.getManyToMany() != null) {
            ManyToMany manyToMany = cm.getManyToMany();
            TableMapping table = MetaHolder.getMeta(clz);
            Class<? extends DBObject> subClz = (Class<? extends DBObject>) manyToMany.targetEntity();
            subClz = (Class<? extends DBObject>) getGenericType(cm, subClz);
            mappedBy = manyToMany.mappedBy();
            //TODO 未实现,自动生成级联中间表的情况
            return doJoinTable(clz, cm, subClz, mappedBy);
        }
        if (cm.getOneToOne() != null) {
            OneToOne oneToOne = cm.getOneToOne();
            TableMapping table = MetaHolder.getMeta(clz);
            Class<? extends DBObject> subClz = (Class<? extends DBObject>) oneToOne.targetEntity();
            subClz = (Class<? extends DBObject>) getGenericType(cm, subClz);
            mappedBy = oneToOne.mappedBy();
            if (StringUtils.isNotBlank(mappedBy)) {
                TableMapping subTable = MetaHolder.getMeta(subClz);
                ColumnMapping subMainTableColumn = subTable.getMetaFieldMap().get(mappedBy);
                return pickCC(clz, cm, subClz, mappedBy, subMainTableColumn);
            }
            CascadeConfig cc = pickCC(clz, cm, subClz, mappedBy, cm);
            if (cc != null) {
                return cc;
            } else {
                //TODO 未实现,自动生成级联中间表的情况
            }
        }
        if (cm.getManyToOne() != null) {
            ManyToOne manyToOne = cm.getManyToOne();
            TableMapping table = MetaHolder.getMeta(clz);
            Class<? extends DBObject> subClz = (Class<? extends DBObject>) manyToOne.targetEntity();
            subClz = (Class<? extends DBObject>) getGenericType(cm, subClz);
            if (cm.getJoinTable() != null) {
                return doJoinTable(clz, cm, subClz, mappedBy);
            } else if (cm.getJoinColumns() != null || cm.getJoinColumn() != null) {
                return doJoinColumns(clz, cm, subClz, mappedBy);
            } else {
                //TODO 未实现,自动生成级联中间表的情况
            }
        }
        if (cm.getOneToMany() != null) {
            OneToMany oneToMany = cm.getOneToMany();
            TableMapping table = MetaHolder.getMeta(clz);
            Class<? extends DBObject> subClz = (Class<? extends DBObject>) oneToMany.targetEntity();
            subClz = (Class<? extends DBObject>) getGenericType(cm, subClz);
            mappedBy = oneToMany.mappedBy();
            if (StringUtils.isNotBlank(mappedBy)) {
                TableMapping subTable = MetaHolder.getMeta(subClz);
                ColumnMapping subMainTableColumn = subTable.getMetaFieldMap().get(mappedBy);
                return pickCC(clz, cm, subClz, mappedBy, subMainTableColumn);
            }
            CascadeConfig cc = pickCC(clz, cm, subClz, mappedBy, cm);
            if (cc != null) {
                return cc;
            } else {
                //TODO 未实现,自动生成级联中间表的情况
            }
        }
        return null;
    }

    /**
     * 生成级联配置
     * @param clz
     * @param cm
     * @param subClz
     * @param mappedBy
     * @param pickColumn 挑拣的列
     * @return
     */
    private static CascadeConfig pickCC(Class<? extends DBObject> clz, ColumnMapping cm, Class<? extends DBObject> subClz, String mappedBy, ColumnMapping pickColumn) {
        if (pickColumn.getJoinTable() != null) {
            return doJoinTable(clz, cm, subClz, mappedBy);
        } else if (pickColumn.getJoinColumns() != null || pickColumn.getJoinColumn() != null) {
            return doJoinColumns(clz, cm, subClz, mappedBy);
        }
        return null;
    }

    private static CascadeConfig doJoinColumns(Class<? extends DBObject> clz, ColumnMapping cm, Class<? extends DBObject> subClz, String mappedBy) {
        TableMapping table = MetaHolder.getMeta(clz);
        subClz = (Class<? extends DBObject>) getGenericType(cm, subClz);
        TableMapping subTable = MetaHolder.getMeta(subClz);
        CascadeConfig cc = new CascadeConfig();
        cc.setFromTable(table);
        cc.setToTable(subTable);
        if (StringUtils.isNotBlank(mappedBy)) {
            //逆向
            //只需生成查询条件,子对象对应主表的列
            ColumnMapping subMainTableColumn = subTable.getMetaFieldMap().get(mappedBy);
            setJoinColumnsDesc(subTable, subMainTableColumn, table, cc);
            //生成子对象查询条件
            {
                selectJoinColumns(cm, subTable, cc);
            }
            cc.setUseMappedBy(true);
            cc.setType(CascadeConfig.LinkType.JoinColumns);
        } else {
            cc.setType(CascadeConfig.LinkType.JoinColumns);
            //正向
            setJoinColumnsDesc(table, cm, subTable, cc);
            //生成子对象查询条件
            {
                selectJoinColumns(cm, subTable, cc);
            }
            //生成插入条件(不需要)

            //生成更新条件(不需要)

            //生成子对象删除条件
            {
                OrmContext ps = new OrmContext();
                List<OrmParameter> parameters = new ArrayList<>();
                StringBuilder sql = new StringBuilder();
                sql.append("delete from " + subTable.getTableName());
                //设置查询条件
                sql.append(" where ");
                boolean f = false;
                for (Map.Entry<ColumnMapping, ColumnMapping> entry : cc.getFromToColumns().entrySet()) {
                    //条件为主表的字段名称
                    sql.append(f ? " and " : "");
                    sql.append(subTable.getTableName() + "." + entry.getValue().getRawColumnName() + " =? ");
                    parameters.add(new OrmParameter().setColumnMapping(entry.getKey()));
                    f = true;
                }
                ps.setSql(sql.toString());
                ps.setParas(parameters);
                cc.setDeleteSubObject(ps);
            }
        }

        return cc;
    }

    private static void selectJoinColumns(ColumnMapping cm, TableMapping subTable, CascadeConfig cc) {
        OrmContext ps = new OrmContext();
        List<OrmParameter> parameters = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        //设置查询列
        List<ColumnMapping> subTableColumns = getFetchDBFieldsColumns(cm, subTable);
        List<String> pStr = new ArrayList<>();// 条件列表
        if (!subTableColumns.isEmpty()) {
            sql.append("select ");
            int size = subTableColumns.size();
            for (int i = 0; i < size; i++) {
                ColumnMapping subColumn = subTableColumns.get(i);
                pStr.add(subTable.getTableName() + "." + subColumn.getRawColumnName());
            }
            sql.append(StringUtils.join(pStr, ","));
            sql.append(" from " + subTable.getTableName());
        } else {
            sql.append("select " + subTable.getTableName() + ".* from " + subTable.getTableName());
        }
        //设置查询条件
        sql.append(" where ");
        pStr.clear();
        for (Map.Entry<ColumnMapping, ColumnMapping> entry : cc.getFromToColumns().entrySet()) {
            if (entry.getValue().getMeta() == subTable) {
                //条件为主表的字段名称,正向条件支持
                pStr.add(subTable.getTableName() + "." + entry.getValue().getRawColumnName() + " =? ");
                parameters.add(new OrmParameter().setColumnMapping(entry.getKey()));
            } else {
                //mappedBy的情况.
                pStr.add(subTable.getTableName() + "." + entry.getKey().getRawColumnName() + " =? ");
                parameters.add(new OrmParameter().setColumnMapping(entry.getValue()));
            }
        }
        sql.append(StringUtils.join(pStr, "and "));
        //处理排序
        OrderByElement[] orders = getOrderBy(cm, subTable);
        if (orders.length > 0) {
            sql.append(" order by ");
            pStr.clear();
            for (int i = 0; i < orders.length; i++) {
                OrderByElement obe = orders[i];
                pStr.add(subTable.getTableName() + "." + obe.expression + " " + (obe.asc ? "asc" : "desc"));
            }
            sql.append(StringUtils.join(pStr, ","));
        }
        ps.setSql(sql.toString());
        ps.setParas(parameters);
        cc.setSelectSubObject(ps);
    }

    /**
     * 生成joinColumns的定义
     * @param table    定义JoinColumn的表
     * @param cm       定义JoinColumn的表的字段
     * @param subTable 子表
     * @return
     */
    private static void setJoinColumnsDesc(TableMapping table, ColumnMapping cm, TableMapping subTable, CascadeConfig cc) {
        Map<ColumnMapping, ColumnMapping> fromToColumns = new HashMap<>();
        if (cm.getJoinColumns() != null) {
            JoinColumn[] joinColumns = cm.getJoinColumns().value();
            for (int i = 0; i < joinColumns.length; i++) {
                JoinColumn jc = joinColumns[i];
                String name = jc.name();
                String referencedColumnName = jc.referencedColumnName();
                ColumnMapping mainTableColumn = table.getSchemaMap().get(table.getFields().get(name));
                ColumnMapping subTableColumn = subTable.getSchemaMap().get(subTable.getFields().get(referencedColumnName));
                fromToColumns.put(mainTableColumn, subTableColumn);
            }
        } else if (cm.getJoinColumn() != null) {
            JoinColumn jc = cm.getJoinColumn();
            String name = jc.name();
            String referencedColumnName = jc.referencedColumnName();
            ColumnMapping mainTableColumn = table.getSchemaMap().get(table.getFields().get(name));
            ColumnMapping subTableColumn = subTable.getSchemaMap().get(subTable.getFields().get(referencedColumnName));
            fromToColumns.put(mainTableColumn, subTableColumn);
        }
        cc.setFromToColumns(fromToColumns);
    }


    private static CascadeConfig doJoinTable(Class<? extends DBObject> clz, ColumnMapping cm, Class<? extends DBObject> subClz, String mappedBy) {
        TableMapping table = MetaHolder.getMeta(clz);
        TableMapping subTable = MetaHolder.getMeta(subClz);
        CascadeConfig cc = new CascadeConfig();
        cc.setFromTable(table);
        cc.setToTable(subTable);
        if (StringUtils.isNotBlank(mappedBy)) {
            //逆向
            //只需生成查询条件,子对象对应主表的列
            ColumnMapping subMainTableColumn = subTable.getMetaFieldMap().get(mappedBy);
            if (subMainTableColumn.getJoinTable() != null) {
                setJoinTableDesc(subTable, subMainTableColumn, table, cc);
                //生成查询条件
                {
                    selectJoinTable(clz, cm, subClz, subTable, cc);
                }
            }
            cc.setUseMappedBy(true);
            cc.setType(CascadeConfig.LinkType.JoinTable);
        } else if (cm.getJoinTable() != null) {
            cc.setType(CascadeConfig.LinkType.JoinTable);
            //正向条件
            setJoinTableDesc(table, cm, subTable, cc);
            //生成查询条件
            {
                selectJoinTable(clz, cm, subClz, subTable, cc);
            }
            //生成插入条件
            {
                OrmContext ps = new OrmContext();
                List<OrmParameter> parameters = new ArrayList<>();
                StringBuilder sql = new StringBuilder();
                sql.append("insert into ").append(cc.getMiddleTableName()).append("(");
                List<String> cStr = new ArrayList<>();// 字段列表
                List<String> vStr = new ArrayList<>();// 值列表
                //处理值
                for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
                    cStr.add(entry.getKey());
                    vStr.add("?");
                    parameters.add(new OrmParameter().setColumnMapping(entry.getValue()));
                }
                sql.append(StringUtils.join(cStr, ",")).append(") values(").append(StringUtils.join(vStr, ",")).append(")");
                ps.setSql(sql.toString());
                ps.setParas(parameters);
                cc.setInsertRelation(ps);
            }
            //生成更新条件(不需要)

            //生成删除关系表条件
            {
                OrmContext ps = new OrmContext();
                List<OrmParameter> parameters = new ArrayList<>();

                StringBuilder sql = new StringBuilder();
                sql.append("delete from ").append(cc.getMiddleTableName()).append(" where ");
                List<String> pStr = new ArrayList<>();// 条件列表
                //处理值
                for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
                    //只添加主表条件即可
                    if (entry.getValue().getMeta().getThisType() == clz) {
                        pStr.add(cc.getMiddleTableName() + "." + entry.getKey() + "=?");
                        parameters.add(new OrmParameter().setColumnMapping(entry.getValue()));
                    }
                }
                sql.append(StringUtils.join(pStr, ","));
                ps.setSql(sql.toString());
                ps.setParas(parameters);
                cc.setDeleteRelation(ps);
            }
            //生成删除子对象表条件
            {
                OrmContext ps = new OrmContext();
                List<OrmParameter> parameters = new ArrayList<>();
                StringBuilder sql = new StringBuilder();
                sql.append("delete " + subTable.getTableName() + " from ").append(subTable.getTableName());
                //设置查询条件
                List<String> expressions = new ArrayList<>();//on 语句
                List<String> conditions = new ArrayList<>();//条件语句
                for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
                    if (entry.getValue().getMeta().getThisType() == clz) {
                        //主表
                        //中间表字段名称,需要根据中间表字段名称,查询结果.
                        conditions.add(cc.getMiddleTableName() + "." + entry.getKey() + "=?");
                        parameters.add(new OrmParameter().setColumnMapping(entry.getValue()));
                    } else if (entry.getValue().getMeta().getThisType() == subClz) {
                        //子表
                        expressions.add(subTable.getTableName() + "." + entry.getValue().getRawColumnName() + "=" + cc.getMiddleTableName() + "." + entry.getKey());
                    }
                }
                sql.append(" inner join " + cc.getMiddleTableName());
                sql.append(" on " + StringUtils.join(expressions, ","));
                sql.append(" where " + StringUtils.join(conditions, ","));
                ps.setSql(sql.toString());
                ps.setParas(parameters);
                cc.setDeleteSubObject(ps);
            }
        }
        return cc;
    }

    /**
     * joinTable 表的查询
     * @param clz
     * @param cm
     * @param subClz
     * @param subTable
     * @param cc
     */
    private static void selectJoinTable(Class<? extends DBObject> clz, ColumnMapping cm, Class<? extends DBObject> subClz, TableMapping subTable, CascadeConfig cc) {
        OrmContext ps = new OrmContext();
        List<OrmParameter> parameters = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        //设置查询列
        List<ColumnMapping> subTableColumns = getFetchDBFieldsColumns(cm, subTable);
        List<String> pStr = new ArrayList<>();// 条件列表
        if (!subTableColumns.isEmpty()) {
            sql.append("select ");
            int size = subTableColumns.size();
            for (int i = 0; i < size; i++) {
                ColumnMapping subColumn = subTableColumns.get(i);
                pStr.add(subTable.getTableName() + "." + subColumn.getRawColumnName());
            }
            sql.append(StringUtils.join(pStr, ","));
            sql.append(" from " + subTable.getTableName() + " " + subTable.getTableName());
        } else {
            sql.append("select " + subTable.getTableName() + ".* from " + subTable.getTableName() + " " + subTable.getTableName());
        }
        //设置查询条件
        List<String> expressions = new ArrayList<>();//on 语句
        List<String> conditions = new ArrayList<>();//条件语句
        for (Map.Entry<String, ColumnMapping> entry : cc.getMiddleTableColumns().entrySet()) {
            if (entry.getValue().getMeta().getThisType() == clz) {
                //主表
                //中间表字段名称,需要根据中间表字段名称,查询结果.
                conditions.add(cc.getMiddleTableName() + "." + entry.getKey() + "=?");
                parameters.add(new OrmParameter().setColumnMapping(entry.getValue()));
            } else if (entry.getValue().getMeta().getThisType() == subClz) {
                //子表
                expressions.add(subTable.getTableName() + "." + entry.getValue().getRawColumnName() + "=" + cc.getMiddleTableName() + "." + entry.getKey());
            }
        }
        sql.append(" inner join " + cc.getMiddleTableName() + " " + cc.getMiddleTableName());
        sql.append(" on " + StringUtils.join(expressions, ","));
        sql.append(" where " + StringUtils.join(conditions, ","));
        //处理排序
        OrderByElement[] orders = getOrderBy(cm, subTable);
        if (orders.length > 0) {
            sql.append(" order by ");
            pStr.clear();
            for (int i = 0; i < orders.length; i++) {
                OrderByElement obe = orders[i];
                pStr.add(subTable.getTableName() + "." + obe.expression + " " + (obe.asc ? "asc" : "desc"));
            }
            sql.append(StringUtils.join(pStr, ","));
        }
        ps.setSql(sql.toString());
        ps.setParas(parameters);
        cc.setSelectSubObject(ps);
    }

    /**
     * @param table    定义JoinTable的表
     * @param cm       定义JoinTable的表的字段
     * @param subTable 子表
     * @return
     */
    private static void setJoinTableDesc(TableMapping table, ColumnMapping cm, TableMapping subTable, CascadeConfig cc) {
        String tableName = cm.getJoinTable().name();
        cc.setMiddleTableName(tableName);
        JoinColumn[] joinColumns = cm.getJoinTable().joinColumns();
        JoinColumn[] inverseJoinColumns = cm.getJoinTable().inverseJoinColumns();
        Map<String, ColumnMapping> middleTableColumns = new LinkedHashMap<>();
        for (int i = 0; i < joinColumns.length; i++) {
            JoinColumn jc = joinColumns[i];
            String name = jc.name();
            String referencedColumnName = jc.referencedColumnName();
            ColumnMapping mainTableColumn = table.getSchemaMap().get(table.getFields().get(referencedColumnName));
            middleTableColumns.put(name, mainTableColumn);
        }
        for (int i = 0; i < inverseJoinColumns.length; i++) {
            JoinColumn jc = inverseJoinColumns[i];
            String name = jc.name();
            String referencedColumnName = jc.referencedColumnName();
            ColumnMapping subTableColumn = subTable.getSchemaMap().get(subTable.getFields().get(referencedColumnName));
            middleTableColumns.put(name, subTableColumn);
        }
        cc.setMiddleTableColumns(middleTableColumns);
    }

    /**
     * 处理抓取字段
     * @param cm
     * @param subTable
     * @return
     */
    private static List<ColumnMapping> getFetchDBFieldsColumns(ColumnMapping cm, TableMapping subTable) {
        FetchDBField fetchDBField = cm.getFetchDBField();
        String[] fetchColumnNames = null;
        List<ColumnMapping> subTableColumns = new ArrayList<>();
        if (fetchDBField != null && fetchDBField.value() != null && fetchDBField.value().length > 0) {
            fetchColumnNames = fetchDBField.value();
            for (String columnName : fetchColumnNames) {
                DBField field = subTable.getFields().get(columnName);
                if (field == null) {
                    throw new RuntimeException("FetchDBField 参数设置有误");
                }
                subTableColumns.add(subTable.getSchemaMap().get(field));
            }
        }
        return subTableColumns;
    }

    /**
     * 获取排序字段排序
     * @param cm
     * @param table 子表
     * @return
     */
    private static OrderByElement[] getOrderBy(ColumnMapping cm, TableMapping table) {
        OrderBy ob = cm.getOrderBy();
        String orderByStr = null;
        List<OrderByElement> list = new ArrayList<>();
        if (ob != null && !ob.value().equals("")) {
            orderByStr = ob.value();
            String[] orderStr1 = orderByStr.split("\\,");
            for (String oneOrder : orderStr1) {
                String[] ones = oneOrder.split(" ");
                String columnName = ones[0];
                String order = null;
                if (ones.length > 1) {
                    order = ones[1];
                }
                DBField dbf = table.getFields().get(columnName);
                ColumnMapping orderColumn = table.getSchemaMap().get(dbf);
                OrderByElement obe;
                if ("desc".equalsIgnoreCase(order)) {
                    obe = OrderByElement.desc(orderColumn.getRawColumnName());
                } else {
                    obe = OrderByElement.asc(orderColumn.getRawColumnName());
                }
                list.add(obe);
            }
        } else {
            for (ColumnMapping pk : table.getPkFields()) {
                list.add(OrderByElement.asc(pk.getRawColumnName()));
            }
        }
        OrderByElement[] orders = new OrderByElement[list.size()];
        return list.toArray(orders);
    }

    static class OrderByElement {

        private String expression;
        private boolean asc = true;

        public OrderByElement() {

        }

        public OrderByElement(String expression, boolean asc) {
            this.expression = expression;
            this.asc = asc;
        }

        public static OrderByElement asc(String expression) {
            return new OrderByElement(expression, true);
        }

        public static OrderByElement desc(String expression) {
            return new OrderByElement(expression, false);
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public boolean isAsc() {
            return asc;
        }

        public void setAsc(boolean asc) {
            this.asc = asc;
        }
    }

    public static <T extends Collection> T convertToCollection(Class<?> clz, List<Object> list) {
        if (clz.isInterface() || Modifier.isAbstract(clz.getModifiers())) {
            if (Set.class.isAssignableFrom(clz)) {
                return (T) new HashSet(list);
            } else {
                return (T) list;
            }
        }
        try {
            T t = (T) clz.newInstance();
            Method addAll = clz.getMethod("addAll", Collection.class);
            ReflectionUtils.invokeMethod(addAll, t, list);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class<?> getGenericType(ColumnMapping cm, Class<?> subClz) {
        if (subClz == null || subClz == void.class) {
            subClz = cm.getClz();
            if (cm.getFieldAccessor().getRawTypes() != null) {
                subClz = cm.getFieldAccessor().getRawTypes()[0];
            }
        }
        return subClz;
    }

    public static void setCollectionValues(DBObject obj, ColumnMapping cm, List subObjs) {
        if (sf.tools.utils.CollectionUtils.isNotEmpty(subObjs)) {
            Object b = convertToCollection(cm.getClz(), subObjs);
            OrmValueUtils.setValue(obj, cm, b);
        }
    }
}
