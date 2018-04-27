package sf.dsl;

import sf.tools.utils.Assert;

public class QueryImpl<T> implements Query<T> {
    private Class<T> clz;

    public QueryImpl(Class<T> clz) {
        Assert.notNull(clz, "");
        this.clz = clz;
    }

    public Class<T> getClz() {
        return clz;
    }

    public void setClz(Class<T> clz) {
        this.clz = clz;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Query from() {
        return null;
    }

    @Override
    public Query forUpdate() {
        return null;
    }

    @Override
    public Query copy() {
        return null;
    }
}
