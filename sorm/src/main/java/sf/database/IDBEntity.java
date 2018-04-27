package sf.database;

import sf.dsl.Query;

import java.io.Serializable;

public interface IDBEntity extends IDBDML, Serializable, AutoCloseable {

    /**
     * 使用查询 废弃
     * @return
     */
    @Deprecated
    default Query<?> useQuery() {
        throw new UnsupportedOperationException();
    }

    /**
     * 有没有带Query对象 废弃
     * @return
     */
    @Deprecated
    default boolean hasQuery() {
        throw new UnsupportedOperationException();
    }

    /**
     * 清除对象中的Query对象，包括延迟加载的钩子等等  废弃
     */
    @Deprecated
    default void clearQuery() {
        throw new UnsupportedOperationException();
    }

    /**
     * 如果是oracle得到rowid
     * @return
     */
    String rowid();

    /**
     * 指定rowid
     * @param rowid
     */
    void bindRowid(String rowid);


    /**
     * 打开字段更新记录开关
     */
    void startUpdate();

    /**
     * 关闭字段更新记录开关
     */
    void stopUpdate();

    /**
     * 判断该字段是否被赋值过
     */
    boolean isUsed(DBField field);

    /**
     * 将某个字段标记为是否赋值过
     * @param field 字段
     * @param flag  true表示这个字段赋值过，false表示没有
     */
    void touchUsedFlag(DBField field, boolean flag);


}