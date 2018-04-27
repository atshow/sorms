package sf.database.jdbc.sql;

import sf.common.log.OrmLog;
import sf.common.wrapper.Page;
import sf.database.*;
import sf.database.dbinfo.DBMetaData;
import sf.database.dialect.DBDialect;
import sf.database.jdbc.handle.PageListHandler;
import sf.database.jdbc.handle.ResultSetHandler;
import sf.database.jdbc.handle.RowListHandler;
import sf.database.jdbc.handle.SingleRowHandler;
import sf.database.jdbc.rowmapper.BeanRowMapper;
import sf.database.jdbc.rowmapper.MapRowMapper;
import sf.database.jdbc.rowmapper.RowMapper;
import sf.database.jdbc.rowmapper.RowMapperHelp;
import sf.database.jdbc.type.TypeHandler;
import sf.database.meta.*;
import sf.database.util.DBUtils;
import sf.database.util.OrmUtils;
import sf.database.util.OrmValueUtils;
import sf.tools.StringUtils;
import sf.tools.utils.Assert;
import sf.tools.utils.CollectionUtils;

import javax.persistence.GenerationType;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 根据model做的增删查改
 * @author sxf
 */
public class CrudModelImpl implements CrudModelInf {

    @Override
    public <T extends DBObject> T selectByPrimaryKeys(Connection conn, Class<T> clz, Object... keyParams) throws SQLException {
        TableMapping table = MetaHolder.getMeta(clz);
        List<ColumnMapping> cmList = table.getPkFields();
        if (cmList.size() != keyParams.length) {
            throw new SQLException("主键参数不对");
        }
        OrmContext context = CommonSql.forModelSelectByIds(table, keyParams);
        RowMapper<T> rowMapper = new BeanRowMapper<>(clz);
        ResultSetHandler<T> rsh = new SingleRowHandler<>(rowMapper);
        T result = select(conn, rsh, context);
        return result;
    }

    public <T> T select(Connection conn, ResultSetHandler<T> rsh, OrmContext context)
            throws SQLException {
        Assert.notNull(rsh, "rsh is null.");
        Assert.notNull(context, "sql is null.");
        PreparedStatement ps = null;
        ResultSet rs = null;
        T result = null;
        OrmLog.commonListLog(context.getSql(), context.getValues());
        //执行时间
        long start = System.currentTimeMillis();
        ps = conn.prepareStatement(context.getSql());
        long setP = System.currentTimeMillis();
        CommonSql.fillOrmStatement(ps, context.getParas());
        System.out.println((System.currentTimeMillis() - setP) + "ms");
        rs = ps.executeQuery();
        try {
            result = rsh.handle(rs);
            OrmLog.resultLog(start, result);
        } catch (Exception e) {
            throw e;
        } finally {
            CommonSql.close(rs);
            CommonSql.close(ps);
        }
        return result;
    }

    static ResultSetHandler<List<Map<String, Object>>> mapRsh = new RowListHandler(new MapRowMapper());

    @Override
    public List<Map<String, Object>> selectListMap(Connection conn, OrmContext context) throws SQLException {
        return select(conn, mapRsh, context);

    }

    @Override
    public <T> T selectOne(Connection conn, Class<T> beanClass, OrmContext context) throws SQLException {
        Assert.notNull(context, "context is null.");
        Assert.notNull(beanClass, "beanClass is null.");

        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);
        ResultSetHandler<T> rsh = new SingleRowHandler<T>(rowMapper);
        return select(conn, rsh, context);
    }

    @Override
    public <T> List<T> selectList(Connection conn, Class<T> beanClass, OrmContext context)
            throws SQLException {
        Assert.notNull(context, "context is null.");
        Assert.notNull(beanClass, "beanClass is null.");
        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);
        ResultSetHandler<List<T>> rsh = new RowListHandler<T>(rowMapper);
        return select(conn, rsh, context);
    }

    /**
     * @param conn
     * @param start     0开始
     * @param limit     限制多少条数据
     * @param beanClass
     * @return
     */
    @Override
    public <T> Page<T> selectPage(Connection conn, int start, int limit, Class<T> beanClass, OrmContext context) throws SQLException {
        Assert.notNull(beanClass, "beanClass is null.");
        RowMapper<T> rowMapper = RowMapperHelp.getRowMapper(beanClass);

        Page<T> page = null;

        String countSql = DBUtils.getSqlSelectCount(context.getSql());
        String sql = context.getSql();
        //设置为查询总数的sql
        context.setSql(countSql);
        long count = selectOne(conn, Long.class, context);
        //改为原来的
        context.setSql(sql);
        page = new Page<T>((int) count, limit);
        List<T> items = Collections.emptyList();
        if (count > 0) {
            String pageSql = DBUtils.doGetDialect(conn, false).sqlPageList(sql, start, limit);
            PageListHandler<T> rsh = new PageListHandler<T>(rowMapper);
            if (pageSql == null) {
                // 如果不支持分页，那么使用原始的分页方法 ResultSet.absolute(first)
                rsh.setFirstResult(start);
            } else {
                // 使用数据库自身的分页SQL语句，将直接返回某一个
                rsh.setFirstResult(0);
                sql = pageSql;
            }
            rsh.setMaxResults(limit);
            context.setSql(sql);
            items = select(conn, rsh, context);
        }
        page.setList(items);
        return page;
    }

    @Override
    public <T extends DBObject> T selectOne(Connection conn, T query) throws SQLException {
        OrmContext select = getQuerySelect(conn, query);
        Class<T> clz = (Class<T>) query.getClass();
        return selectOne(conn, clz, select);
    }

    private <T extends DBObject> OrmContext getQuerySelect(Connection conn, T query) {
        OrmContext ctx = new OrmContext();
        StringBuilder sql = new StringBuilder();
        TableMapping tm = MetaHolder.getMeta(query.getClass());
        sql.append("select * from " + tm.getTableName());
        List<OrmParameter> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(query.updateValueMap())) {
            sql.append(" where ");
            boolean f = false;
            for (Map.Entry<DBField, Object> entry : query.updateValueMap().entrySet()) {
                ColumnMapping cm = tm.getSchemaMap().get(entry.getKey());
                list.add(new OrmParameter(entry.getValue(), cm));
                sql.append(f ? " and " : "").append(cm.getRawColumnName() + " =? ");
                f = true;
            }
        }
        ctx.setSql(sql.toString());
        ctx.setParas(list);
        query.clearUpdate();
        return ctx;
    }

    @Override
    public <T extends DBObject> List<T> selectList(Connection conn, T query) throws SQLException {
        OrmContext select = getQuerySelect(conn, query);
        Class<T> clz = (Class<T>) query.getClass();
        return selectList(conn, clz, select);
    }

    @Override
    public <T extends DBObject> Page<T> selectPage(Connection conn, T query, int start, int limit) throws SQLException {
        OrmContext select = getQuerySelect(conn, query);
        Class<T> clz = (Class<T>) query.getClass();
        return selectPage(conn, start, limit, clz, select);
    }

    @Override
    public <T extends DBObject> void selectIterator(Connection conn, OrmIterator<T> ormIt, T query) throws SQLException {
        OrmContext select = getQuerySelect(conn, query);
        Object[] vals = null;
        if (!select.getValues().isEmpty()) {
            vals = select.getValues().toArray();
        }
        Crud.getInstance().getCrudSql().selectIterator(conn, ormIt, (Class<T>) query.getClass(), select.getSql(), vals);
    }

    @Override
    public <T extends DBObject> void selectStream(Connection conn, OrmStream<T> ormStream, T query) throws SQLException {
        OrmContext select = getQuerySelect(conn, query);
        Object[] vals = null;
        if (!select.getValues().isEmpty()) {
            vals = select.getValues().toArray();
        }
        Crud.getInstance().getCrudSql().selectStream(conn, ormStream, (Class<T>) query.getClass(), select.getSql(), vals);
    }

    @Override
    public int insert(Connection conn, DBObject obj) throws SQLException {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        DBDialect dialect = DBUtils.doGetDialect(conn, false);
        OrmContext context = CommonSql.model2Save(table, dialect, obj, true);

        OrmLog.commonListLog(context.getSql(), context.getValues());

        List<ColumnMapping> cmList = table.getPkFields();
        String[] pKeys = null;
        if (CollectionUtils.isNotEmpty(cmList)) {
            pKeys = new String[cmList.size()];
            for (int i = 0; i < cmList.size(); i++) {
                pKeys[i] = cmList.get(i).getRawColumnName();
            }
        }

        //处理自定义主键
        for (OrmParameter p : context.getParas()) {
            Object value = p.getValue();
            if (value == null) {
                IdentifierGenerator ig = p.getColumnMapping().getIdentifierGenerator();
                if (ig != null) {
                    value = ig.generate(conn, p.getColumnMapping());
                }
                p.setValue(value);
            }
        }

        //执行时间
        long start = System.currentTimeMillis();
        PreparedStatement pst = null;
        if (cmList != null && cmList.size() == 1 && cmList.get(0).getGv().strategy() == GenerationType.AUTO) {
            pst = conn.prepareStatement(context.getSql(), Statement.RETURN_GENERATED_KEYS);
        } else {
            pst = conn.prepareStatement(context.getSql(), pKeys);
        }
        CommonSql.fillOrmStatement(pst, context.getParas());
        obj.clearUpdate();
        int result = pst.executeUpdate();
        getGeneratedKey(pst, obj, pKeys);
        obj.clearUpdate();
        OrmLog.resultLog(start, result);
        CommonSql.close(pst);
        return result;
    }

    @Override
    public int insertCascade(Connection conn, DBObject obj, DBCascadeField... fields) throws SQLException {
        int count = insert(conn, obj);
        insertLinks(conn, obj, fields);
        return count;
    }

    /**
     * Get id after save record.
     */
    private void getGeneratedKey(PreparedStatement pst, DBObject obj, String[] pKeys)
            throws SQLException {
        ResultSet rs = pst.getGeneratedKeys();
        TableMapping tm = MetaHolder.getMeta(obj.getClass());
        if (rs != null) {
            if (rs.next()) {
                int i = 1;
                for (String pKey : pKeys) {
                    ColumnMapping cm = tm.getMetaFieldMap().get(pKey);
                    TypeHandler<?> type = cm.getHandler();
                    Object val = type.get(rs, i++);
                    OrmValueUtils.setValue(obj, cm, val);
                }
            }
            rs.close();
        }

    }

    /*
     * Execute sql update
     */
    @Override
    public int update(Connection conn, DBObject obj) throws SQLException {
        //不开启乐观锁校验
        OrmContext context = update(conn, obj, false);
        return (Integer) context.getResult();
    }

    /**
     * @param conn
     * @param obj
     * @param useOptimisticLock 是否使用乐观锁
     * @return
     * @throws SQLException
     */
    protected OrmContext update(Connection conn, DBObject obj, boolean useOptimisticLock) throws SQLException {
        TableMapping table = MetaHolder.getMeta(obj.getClass());

        OrmContext context = CommonSql.forModelUpdate(table, obj, useOptimisticLock);
        String sql = context.getSql();
        List<OrmParameter> paras = context.getParas();
        if (paras.size() <= 1) { // Needn't update
            context.setResult(paras.size());
            return context;
        }
        //日志
        OrmLog.commonListLog(sql, context.getValues());
        //执行时间
        long start = System.currentTimeMillis();
        PreparedStatement pst = conn.prepareStatement(sql);
        CommonSql.fillOrmStatement(pst, context.getParas());
        int result = pst.executeUpdate();
        OrmLog.resultLog(start, result);
        CommonSql.close(pst);
        obj.clearUpdate();
        context.setResult(result);
        return context;
    }

    @Override
    public <T extends DBObject> int updateAndSet(Connection conn, T obj) throws SQLException {
        OrmContext context = update(conn, obj, true);
        Integer result = (Integer) context.getResult();
        if (result > 0) {
            TableMapping tm = MetaHolder.getMeta(obj.getClass());
            Map<DBField, ColumnMapping> versionMap = tm.getVersionMap();
            boolean dateVersion = false;
            for (Map.Entry<DBField, ColumnMapping> entry : versionMap.entrySet()) {
                if (Date.class.isAssignableFrom(entry.getValue().getClz())) {
                    dateVersion = true;
                    break;
                }
            }
            //更新成功,则变更对象的版本值
            if (dateVersion) {
                //对于日期,乐观锁最新的值需要从数据库获取.
                OptimisticLock.setNewOptimisticLockValues(conn, obj);
            } else {
                List<OrmParameter> preResult = context.getPreResultParas();
                if (CollectionUtils.isNotEmpty(preResult)) {
                    for (OrmParameter v : preResult) {
                        OrmValueUtils.setValue(obj, v.getColumnMapping(), v.getValue());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T extends DBObject> int updateWithVersion(Connection conn, T obj) throws SQLException {
        OrmContext context = update(conn, obj, true);
        return (Integer) context.getResult();
    }

    @Override
    public int updateCascade(Connection conn, DBObject obj, DBCascadeField... fields) throws SQLException {
        updateLinks(conn, obj, fields);
        return update(conn, obj);
    }

    @Override
    public int merge(Connection conn, DBObject obj) throws SQLException {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<ColumnMapping> cmList = table.getPkFields();
        DBField[] fields = new DBField[cmList.size()];
        boolean exist = true;
        for (int i = 0; i < cmList.size(); i++) {
            fields[i] = cmList.get(i).getField();
        }
        Object[] values = OrmUtils.getDataObjectValues(obj, fields);
        for (Object val : values) {
            if (val == null) {
                exist = false;
            }
        }
        DBObject temp = null;
        if (exist) {
            temp = selectByPrimaryKeys(conn, obj.getClass(), values);
        }
        int i = 0;
        if (temp == null) {
            i = insert(conn, obj);
        } else {
            i = update(conn, obj);
        }
        return i;
    }

    /*
     * Execute sql update
     */
    @Override
    public int delete(Connection conn, DBObject obj) throws SQLException {
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<OrmParameter> paras = new ArrayList<>();
        List<ColumnMapping> cmList = table.getPkFields();
        OrmContext context = CommonSql.forModelDeleteById(table, obj);
        OrmLog.commonListLog(context.getSql(), context.getValues());
        //执行时间
        long start = System.currentTimeMillis();
        PreparedStatement pst = conn.prepareStatement(context.getSql());
        CommonSql.fillOrmStatement(pst, context.getParas());
        int result = pst.executeUpdate();
        OrmLog.resultLog(start, result);
        CommonSql.close(pst);
        return result;
    }

    @Override
    public int deleteCascade(Connection conn, DBObject obj, DBCascadeField... fields) throws SQLException {
        deleteLinks(conn, obj, fields);
        return delete(conn, obj);
    }

    /**
     * Batch save models using the "insert into ..." sql generated by the first
     * model in modelList. Ensure all the models can use the same sql as the
     * first model.
     */
    public int[] batchInsert(Connection conn, Collection<? extends DBObject> modelList, boolean insertFast, int batchSize) throws SQLException {
        if (modelList == null || modelList.size() == 0)
            return new int[0];
        DBObject data = modelList.iterator().next();
        TableMapping table = MetaHolder.getMeta(data.getClass());
        DBDialect dialect = DBUtils.doGetDialect(conn, false);
        OrmContext context = CommonSql.model2Save(table, dialect, data, true);
        return batch(conn, context, modelList, insertFast, batchSize);
    }

    /**
     * Batch update models using the attrs names of the first model in
     * modelList. Ensure all the models can use the same sql as the first model.
     */
    @Override
    public int[] batchUpdate(Connection conn, Collection<? extends DBObject> modelList, int batchSize) throws SQLException {
        if (modelList == null || modelList.size() == 0)
            return new int[0];

        DBObject data = modelList.iterator().next();
        TableMapping table = MetaHolder.getMeta(data.getClass());
        OrmContext context = CommonSql.forModelUpdate(table, data, true);
        return batch(conn, context, modelList, false, batchSize);
    }

    /**
     * Batch delete records using the columns names of the first record in
     * recordList. Ensure all the records can use the same sql as the first
     * record.
     * @param modelList the table name
     * @param batchSize the primary key of the table, composite primary key is
     *                  separated by comma character: ","
     */
    @Override
    public int[] batchDelete(Connection conn, Collection<? extends DBObject> modelList, int batchSize) throws SQLException {
        if (sf.tools.utils.CollectionUtils.isEmpty(modelList)) {
            return new int[0];
        }
        DBObject data = modelList.iterator().next();
        TableMapping table = MetaHolder.getMeta(data.getClass());
        OrmContext context = CommonSql.forModelDeleteById(table, data);
        return batch(conn, context, modelList, false, batchSize);
    }

    /**
     * @param conn
     * @param context
     * @param list
     * @param insertFast 快速插入标志
     * @param batchSize
     * @return
     * @throws SQLException
     */
    private int[] batch(Connection conn, OrmContext context, Collection<? extends DBObject> list, boolean insertFast,
                        int batchSize) throws SQLException {
        String sql = context.getSql();
        List<OrmParameter> columns = context.getParas();
        if (list == null || list.size() == 0)
            return new int[0];
        if (batchSize < 1)
            throw new IllegalArgumentException("The batchSize must more than 0.");

        boolean isInsert = false;
        boolean isInertAuto = false;
        boolean isUpdate = false;
        boolean isDelete = false;
        if (StringUtils.containsIgnoreCase(sql, "insert")) {
            isInsert = true;
        } else if (StringUtils.containsIgnoreCase(sql, "update")) {
            isUpdate = true;
        } else if (StringUtils.containsIgnoreCase(sql, "delete")) {
            isDelete = true;
        }
        int counter = 0;
        int pointer = 0;
        int size = list.size();
        int[] result = new int[size];
        TableMapping table = MetaHolder.getMeta(list.iterator().next().getClass());
        List<ColumnMapping> pkFields = table.getPkFields();
        String[] pKeys = null;
        if (CollectionUtils.isNotEmpty(pkFields)) {
            pKeys = new String[pkFields.size()];
            int i = 0;
            for (ColumnMapping cm : pkFields) {
                pKeys[i++] = cm.getRawColumnName();
            }
        }
        //执行时间
        long start = System.currentTimeMillis();
        PreparedStatement pst = null;
        if (isInsert && !insertFast) {
            if (pkFields != null && pkFields.size() == 1 &&
                    (pkFields.get(0).getGv().strategy() == GenerationType.AUTO || pkFields.get(0).getGv().strategy() == GenerationType.IDENTITY)) {
                pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                isInertAuto = true;
            } else {
                pst = conn.prepareStatement(sql, pKeys);
            }
        } else {
            pst = conn.prepareStatement(sql);
        }
        for (DBObject data : list) {
            Map<DBField, Object> map = data.updateValueMap();
            int j = 0;
            //记录日志
            List<Object> values = new ArrayList<>();
            for (OrmParameter p : columns) {
                Object value = map.get(p.getField());
                if (value == null && isInsert) {
                    //处理自定义主键
                    IdentifierGenerator ig = p.getColumnMapping().getIdentifierGenerator();
                    if (ig != null) {
                        value = ig.generate(conn, p.getColumnMapping());
                    }
                }
                if (value == null) {
                    value = OrmValueUtils.getValue(data, p.getColumnMapping());
                }
                ColumnMapping column = p.getColumnMapping();
                column.getHandler().set(pst, value, ++j);
                values.add(value);
            }
            OrmLog.batchCommonLog(sql, list.size(), counter + 1, values);
            pst.addBatch();
            if (++counter >= batchSize) {
                counter = 0;
                int[] r = pst.executeBatch();
                for (int k = 0; k < r.length; k++) {
                    result[pointer++] = r[k];
                }
            }
        }
        int[] r = pst.executeBatch();
        for (int k = 0; k < r.length; k++) {
            result[pointer++] = r[k];
        }
        if (isInertAuto && !insertFast) {
            setPkValueAfter(pst, table, list, pKeys);
        }
        OrmLog.resultLog(start, result);
        CommonSql.close(pst);
        for (DBObject data : list) {
            data.clearUpdate();
        }
        return result;
    }

    /**
     * Get id after save record.
     */
    private void setPkValueAfter(PreparedStatement pst, TableMapping tm, Collection<? extends DBObject> objs,
                                 String[] pKeys) throws SQLException {
        ResultSet rs = pst.getGeneratedKeys();
        Iterator<? extends DBObject> it = objs.iterator();
        if (rs != null) {
            while (rs.next()) {
                int i = 1;
                DBObject o = it.next();
                for (String pKey : pKeys) {
                    ColumnMapping cm = tm.getMetaFieldMap().get(pKey);
                    TypeHandler<?> type = cm.getHandler();
                    Object val = type.get(rs, i++);
                    OrmValueUtils.setValue(o, cm, val);
                }
            }
            rs.close();
        }
    }

    @Override
    public boolean createTable(Connection conn, Class<?> clz) {
        TableMapping en = MetaHolder.getMeta(clz);
        DBDialect dialect = DBUtils.doGetDialect(conn, false);
        try {
            if (!DBMetaData.getInstance().existTable(conn, en.getTableName())) {
                return dialect.createEntity(conn, en);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public DBObject fetchLinks(Connection conn, DBObject obj) throws SQLException {
        return fetchLinks(conn, obj, (String[]) null);
    }

    @Override
    public <T extends DBObject> T fetchLinks(Connection conn, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        String[] fed = null;
        if (fields != null) {
            fed = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                fed[i] = fields[i].name();
            }
        }
        return fetchLinks(conn, obj, fed);
    }

    public <T extends DBObject> T fetchLinks(Connection conn, T obj, String... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<ColumnMapping> columnMappings = table.getMetaFields();
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                String field = fields[i];
                ColumnMapping cm = table.getSchemaMap().get(table.getFields().get(field));
                if (cm != null) {
                    fetchSubJoin(conn, obj, cm);
                }
            }
        } else {
            for (ColumnMapping cm : columnMappings) {
                fetchSubJoin(conn, obj, cm);
            }
        }
        return obj;
    }

    private <T extends DBObject> void fetchSubJoin(Connection conn, T obj, ColumnMapping cm) throws SQLException {
        if (cm.getCascadeConfig() != null) {
            if (cm.getCascadeConfig().getType() == CascadeConfig.LinkType.JoinTable) {
                fetchSubJoinTables(conn, obj, cm);
            } else if (cm.getCascadeConfig().getType() == CascadeConfig.LinkType.JoinColumns) {
                fetchSubJoinColumns(conn, obj, cm);
            }
        }
    }

    private void fetchSubJoinTables(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc == null) {
            MetaHolder.cascade(MetaHolder.getMeta(obj.getClass()), cm);
        }
        OrmContext select = cc.getSelectSubObject();
        List<OrmParameter> mainTableParas = select.getParas();
        for (OrmParameter val : mainTableParas) {
            ColumnMapping from = val.getColumnMapping();
            Object value = OrmValueUtils.getValue(obj, from);
            val.setValue(value);
        }
        List subObjs = selectList(conn, cc.getToTable().getThisType(), select);
        if (Collection.class.isAssignableFrom(cm.getClz())) {
            CascadeUtils.setCollectionValues(obj, cm, subObjs);
        } else {
            //设置单一值
            if (CollectionUtils.isNotEmpty(subObjs)) {
                Object val = subObjs.get(0);
                OrmValueUtils.setValue(obj, cm, val);
            }
        }
    }

    private void fetchSubJoinColumns(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        List subObjs = getSubObjectList(conn, obj, cm);
        if (sf.tools.utils.CollectionUtils.isNotEmpty(subObjs)) {
            if (Collection.class.isAssignableFrom(cm.getClz())) {
                //集合
                CascadeUtils.setCollectionValues(obj, cm, subObjs);
            } else {
                //单一值
                //设置单一值
                if (CollectionUtils.isNotEmpty(subObjs)) {
                    Object val = subObjs.get(0);
                    OrmValueUtils.setValue(obj, cm, val);
                }
            }
        }
    }


    private List getSubObjectList(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc == null) {
            MetaHolder.cascade(MetaHolder.getMeta(obj.getClass()), cm);
        }
        Class<?> subClz = cc.getToTable().getThisType();
        OrmContext select = cc.getSelectSubObject();
        List<OrmParameter> mainTableParas = select.getParas();
        for (OrmParameter val : mainTableParas) {
            ColumnMapping from = val.getColumnMapping();
            Object value = OrmValueUtils.getValue(obj, from);
            val.setValue(value);
        }
        return selectList(conn, subClz, select);
    }


    @Override
    public <T extends DBObject> T insertLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<ColumnMapping> columnMappings = table.getMetaFields();
        int count = 0;
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                DBCascadeField field = fields[i];
                ColumnMapping cm = table.getMetaFieldMap().get(field.name());
                if (cm != null) {
                    count += insertLink(con, obj, cm);
                }
            }
        } else {
            for (ColumnMapping cm : columnMappings) {
                count += insertLink(con, obj, cm);
            }
        }
        return null;
    }

    private <T extends DBObject> int insertLink(Connection con, T obj, ColumnMapping cm) throws SQLException {
        int count = 0;
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc != null) {
            //插入子对象
            count = insertSubObject(con, obj, cm);
            if (cc.getInsertRelation() != null) {
                //插入关联关系
                insertRelation(con, obj, cm.getCascadeField());
            }
        }
        return count;
    }

    private int insertSubObject(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        Object subObject = OrmValueUtils.getValue(obj, cm);
        CascadeConfig cc = cm.getCascadeConfig();
        Map<ColumnMapping, ColumnMapping> fromToColumns = null;
        if (cc != null && !cc.isUseMappedBy()) {
            fromToColumns = cc.getFromToColumns();
        }
        int count = 0;
        if (subObject != null) {
            if (Collection.class.isAssignableFrom(subObject.getClass())) {
                Collection collection = (Collection) subObject;
                for (Object sub : collection) {
                    setSubJoinColumnValue(obj, fromToColumns, sub);
                    count += insert(conn, (DBObject) sub);
                }
            } else {
                setSubJoinColumnValue(obj, fromToColumns, subObject);
                count += insert(conn, (DBObject) subObject);
            }
        }
        return count;
    }

    /**
     * 设置子对象的关联字段值
     * @param obj
     * @param fromToColumns
     * @param sub
     */
    private void setSubJoinColumnValue(DBObject obj, Map<ColumnMapping, ColumnMapping> fromToColumns, Object sub) {
        if (fromToColumns != null) {
            for (Map.Entry<ColumnMapping, ColumnMapping> entry : fromToColumns.entrySet()) {
                Object joinColumnValue = OrmValueUtils.getValue(obj, entry.getKey());
                try {
                    entry.getValue().getFieldAccessor().getSetter().invoke(sub, joinColumnValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public <T extends DBObject> T insertRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        Assert.notNull(fields, "字段不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        for (int i = 0; i < fields.length; i++) {
            ColumnMapping cm = table.getMetaFieldMap().get(fields[i].name());
            if (cm != null) {
                insertRelation(con, obj, cm);
            } else {
                throw new RuntimeException("字段未找到");
            }
        }
        return obj;
    }

    private int insertRelation(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        int count = 0;
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc == null) {
            MetaHolder.cascade(MetaHolder.getMeta(obj.getClass()), cm);
        }
        Collection subObjects = OrmValueUtils.getValue(obj, cm);
        if (subObjects == null) {
            return count;
        }
        OrmContext insert = cc.getInsertRelation();
        String sql = insert.getSql();
        List<OrmParameter> mainTableParas = insert.getParas();
        //设置关系表的值,几个子对象,设置几个子对象的关系.
        List<Object[]> relationsValues = new ArrayList<>();
        for (int i = 0; i < subObjects.size(); i++) {
            Object[] relationValues = new Object[cc.getMiddleTableColumns().size()];
            relationsValues.add(relationValues);
        }
        int i = 0;
        for (OrmParameter val : mainTableParas) {
            ColumnMapping column = val.getColumnMapping();
            if (column.getMeta().getThisType() == cc.getFromTable().getThisType()) {
                Object value = OrmValueUtils.getValue(obj, column);
                for (int j = 0; j < subObjects.size(); j++) {
                    relationsValues.get(j)[i] = value;
                }
            } else if (column.getMeta().getThisType() == cc.getToTable().getThisType()) {
                int k = 0;
                for (Object sub : subObjects) {
                    Object subValue = OrmValueUtils.getValue(sub, column);
                    relationsValues.get(k)[i] = subValue;
                    k++;
                }
            }
            i++;
        }
        int[] all = Crud.getInstance().getCrudSql().executeBatch(conn, sql, relationsValues);
        if (all != null) {
            for (int a : all) {
                count += a;
            }
        }
        return count;
    }

    @Override
    public <T extends DBObject> T updateLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<ColumnMapping> columnMappings = table.getMetaFields();
        int count = 0;
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                DBCascadeField field = fields[i];
                ColumnMapping cm = table.getMetaFieldMap().get(field.name());
                if (cm != null) {
                    count += updateLink(con, obj, cm);
                }
            }
        } else {
            for (ColumnMapping cm : columnMappings) {
                count += updateLink(con, obj, cm);
            }
        }
        return null;
    }

    private <T extends DBObject> int updateLink(Connection con, T obj, ColumnMapping cm) throws SQLException {
        int count = 0;
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc != null) {
            //更新子对象
            count = updateSubObject(con, obj, cm);
            //可以判断是JoinTable关系
            if (cc.getInsertRelation() != null) {
                //更新关联关系
                updateRelation(con, obj, cm.getCascadeField());
            }
        }
        return count;
    }

    /**
     * @param conn
     * @param obj
     * @param cm
     * @return
     * @throws SQLException
     */
    private int updateSubObject(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        Object subObject = OrmValueUtils.getValue(obj, cm);
        CascadeConfig cc = cm.getCascadeConfig();
        Map<ColumnMapping, ColumnMapping> fromToColumns = null;
        if (cc != null && !cc.isUseMappedBy()) {
            fromToColumns = cc.getFromToColumns();
        }
        int count = 0;
        if (subObject != null) {
            if (Collection.class.isAssignableFrom(subObject.getClass())) {
                Collection collection = (Collection) subObject;
                for (Object sub : collection) {
                    setSubJoinColumnValue(obj, fromToColumns, sub);
                    count += update(conn, (DBObject) sub);
                }
            } else {
                setSubJoinColumnValue(obj, fromToColumns, subObject);
                count += update(conn, (DBObject) subObject);
            }
        }
        return count;
    }

    @Override
    public <T extends DBObject> int updateRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        Assert.notNull(fields, "字段不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());

        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            ColumnMapping cm = table.getMetaFieldMap().get(fields[i].name());
            if (cm != null) {
                Object subObject = OrmValueUtils.getValue(obj, cm);
                count += updateRelation(con, obj, cm, subObject);
            } else {
                throw new RuntimeException("字段未找到");
            }
        }
        return count;
    }

    /**
     * @param con
     * @param obj       主对象
     * @param cm        obj的级联字段
     * @param subObject 子对象
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T extends DBObject> int updateRelation(Connection con, T obj, ColumnMapping cm, Object subObject) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        Assert.notNull(cm, "字段不能为null!");
        int count = 0;
        if (subObject != null) {
            deleteRelation(con, obj, cm);
            count += insertRelation(con, obj, cm);
        }
        return count;
    }

    /**
     * @param con
     * @param obj    数据对象
     * @param fields 正则表达式，描述了什么样的关联字段将被关注。如果为 null，则表示全部的关联字段都会被删除
     * @param <T>
     * @return
     */
    @Override
    public <T extends DBObject> int deleteLinks(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        List<ColumnMapping> columnMappings = table.getMetaFields();
        int count = 0;
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                DBCascadeField field = fields[i];
                ColumnMapping cm = table.getMetaFieldMap().get(field.name());
                if (cm != null) {
                    count += deleteLink(con, obj, cm);
                }
            }
        } else {
            for (ColumnMapping cm : columnMappings) {
                count += deleteLink(con, obj, cm);
            }
        }
        return count;
    }

    private <T extends DBObject> int deleteLink(Connection con, T obj, ColumnMapping cm) throws SQLException {
        int count = 0;
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc != null && cc.getDeleteSubObject() != null) {
            //删除子对象
            count = deleteSubObject(con, obj, cm);
            if (cc.getDeleteRelation() != null) {
                //删除关联关系
                count += deleteRelation(con, obj, cm);
            }
        }
        return count;
    }

    private int deleteSubObject(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc == null) {
            MetaHolder.cascade(MetaHolder.getMeta(obj.getClass()), cm);
        }
        OrmContext delete = cc.getDeleteSubObject();
        String sql = delete.getSql();
        List<OrmParameter> mainTableParas = delete.getParas();
        //设置主表的值
        List<Object> relationValues = new ArrayList<>();
        for (OrmParameter val : mainTableParas) {
            ColumnMapping column = val.getColumnMapping();
            Object value = OrmValueUtils.getValue(obj, column);
            relationValues.add(value);
        }
        return Crud.getInstance().getCrudSql().execute(conn, sql, relationValues.toArray());
    }

    /**
     * 多对多
     * @param con
     * @param obj
     * @param fields 正则表达式，描述了那种多对多关联字段将被执行该操作
     * @param <T>
     * @return
     * @throws SQLException
     */
    @Override
    public <T extends DBObject> int deleteRelation(Connection con, T obj, DBCascadeField... fields) throws SQLException {
        Assert.notNull(obj, "对象不能为null!");
        Assert.notNull(fields, "字段不能为null!");
        TableMapping table = MetaHolder.getMeta(obj.getClass());
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            ColumnMapping cm = table.getMetaFieldMap().get(fields[i].name());
            if (cm != null) {
                count += deleteRelation(con, obj, cm);
            } else {
                throw new RuntimeException("字段未找到");
            }
        }
        return count;
    }

    private int deleteRelation(Connection conn, DBObject obj, ColumnMapping cm) throws SQLException {
        CascadeConfig cc = cm.getCascadeConfig();
        if (cc == null) {
            MetaHolder.cascade(MetaHolder.getMeta(obj.getClass()), cm);
        }
        OrmContext delete = cc.getDeleteRelation();
        String sql = delete.getSql();
        List<OrmParameter> mainTableParas = delete.getParas();
        //设置主表的值
        List<Object> relationValues = new ArrayList<>();
        for (OrmParameter val : mainTableParas) {
            ColumnMapping column = val.getColumnMapping();
            Object value = OrmValueUtils.getValue(obj, column);
            relationValues.add(value);
        }
        return Crud.getInstance().getCrudSql().execute(conn, sql, relationValues.toArray());
    }
}
