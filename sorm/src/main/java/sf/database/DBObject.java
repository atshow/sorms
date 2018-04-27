package sf.database;

import sf.database.meta.ColumnMapping;
import sf.database.meta.MetaHolder;
import sf.database.meta.TableMapping;

import javax.persistence.PersistenceException;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * 抽象类，用于实现所有Entity默认的各种方法
 */
@SuppressWarnings("serial")
@XmlTransient
public abstract class DBObject implements IDBEntity {
    private transient Map<DBField, Object> updateValueMap;
    protected transient boolean _recordUpdate = true;
    private transient String _rowid;
    transient ILazyLoadContext lazyload;

    private static final ConditionComparator cmp = new ConditionComparator();

    /**
     * 是否使用tail属性,已废弃.
     */
//    private transient boolean useTail;

    /**
     * 额外属性(查询时未转换为bean属性的额外数据库字段)
     */
    private Map<String, Object> tail;


//    protected transient Query<?> query;

    /**
     * 使用查询
     * @return
     */
//    public Query<?> useQuery() {
//        if (query == null)
//            query = new QueryImpl(this.getClass());
//        return query;
//    }

//    @Override
//    public boolean hasQuery() {
//        return query != null;
//    }

//    @Override
//    public final void clearQuery() {
//        query = null;
//        lazyload = null;
//    }
    @Override
    public final void startUpdate() {
        _recordUpdate = true;
    }

    @Override
    public final void stopUpdate() {
        _recordUpdate = false;
    }

    @Override
    public final boolean isUsed(DBField field) {
        if (updateValueMap == null) {
            return false;
        }
        return updateValueMap.containsKey(field);
    }

    @Override
    public final void clearUpdate() {
        updateValueMap = null;
    }

    @Override
    public final Map<DBField, Object> updateValueMap() {
        if (updateValueMap == null) {
            return Collections.emptyMap();
        }
        return updateValueMap;
    }

    @Override
    public final void prepareUpdate(DBField field, Object newValue) {
        if (updateValueMap == null) {
            updateValueMap = new TreeMap<DBField, Object>(cmp);
        }
        updateValueMap.put(field, newValue);

    }

    void markUpdateFlag(DBField field, Object newValue) {
        if (updateValueMap == null) {
            updateValueMap = new TreeMap<DBField, Object>(cmp);
        }
        updateValueMap.put(field, newValue);

    }

    @Override
    public final void applyUpdate() {
        if (updateValueMap == null)
            return;
        for (Entry<DBField, Object> entry : updateValueMap.entrySet()) {
            Object newValue = entry.getValue();
            if (newValue instanceof DBField) {
                continue;
            }
        }
        clearUpdate();
    }

    @Override
    public final boolean needUpdate() {
        return (updateValueMap != null) && this.updateValueMap.size() > 0;
    }

    @Override
    public String rowid() {
        return _rowid;
    }

    @Override
    public void bindRowid(String rowid) {
        this._rowid = rowid;
    }

    /*
     * 供子类hashCode（）方法调用，判断内嵌的hashCode方法是否可用
     */
    protected final int getHashCode() {
        int result = updateValueMap != null ? updateValueMap.hashCode() : 0;
        result = 31 * result + (_recordUpdate ? 1 : 0);
        result = 31 * result + (_rowid != null ? _rowid.hashCode() : 0);
        result = 31 * result + (tail != null ? tail.hashCode() : 0);
        return result;
    }

    protected final void beforeSet(String fieldname) {
        if (lazyload == null)
            return;
        lazyload.markProcessed(fieldname);
    }

    /*
     * 处理延迟加载的字段
     */
    protected final void beforeGet(String fieldname) {
        if (lazyload == null)
            return;
        int id = lazyload.needLoad(fieldname);
        if (id > -1) {
            try {
                if (lazyload.process(this, id)) {
                    lazyload = null; // 清理掉，以后不再需要延迟加载
                }
            } catch (SQLException e) {
                throw new PersistenceException(e);
            }
        }
    }

    /*
     * 供子类的equals方法调用，判断内嵌的query对象、updateMap对象是否相等
     */
    protected final boolean isEquals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DBObject that = (DBObject) obj;

        if (_recordUpdate != that._recordUpdate) return false;
        if (updateValueMap != null ? !updateValueMap.equals(that.updateValueMap) : that.updateValueMap != null)
            return false;
        return true;
    }

    private static class ConditionComparator implements Comparator<DBField>, Serializable {
        @Override
        public int compare(DBField o1, DBField o2) {
            if (o1 == o2)
                return 0;
            if (o1 == null)
                return 1;
            if (o2 == null)
                return -1;
            return o1.name().compareTo(o2.name());
        }
    }

    @Override
    public void touchUsedFlag(DBField field, boolean flag) {
        if (flag) {
            if (updateValueMap == null)
                updateValueMap = new TreeMap<DBField, Object>(cmp);
            if (updateValueMap.containsKey(field)) {
                return;
            }
            TableMapping meta = MetaHolder.getMeta(this.getClass());
            ColumnMapping ba = meta.getSchemaMap().get(field);
            updateValueMap.put(field, ba.getFieldAccessor().get(this));
        } else {
            if (updateValueMap != null) {
                updateValueMap.remove(field);
            }
        }
    }

    public Map<String, Object> getTail() {
        return tail;
    }

    public void setTail(Map<String, Object> tail) {
        this.tail = tail;
    }

    @Override
    public void close() throws Exception {

    }
}
