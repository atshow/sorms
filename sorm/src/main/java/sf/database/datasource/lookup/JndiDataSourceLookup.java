package sf.database.datasource.lookup;

import sf.common.log.LogUtil;
import sf.database.datasource.DataSourceLookup;
import sf.database.datasource.RoutingDataSource;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JndiDataSourceLookup implements DataSourceLookup {
    private InitialContext ctx;
    private String defaultKey;
    private String namePrifix = "";

    public DataSource getDataSource(String dataSourceName) {
        if (ctx == null) {
            init();
        }
        try {
            DataSource ds = (DataSource) ctx.lookup(namePrifix + dataSourceName);
            return ds;// 包装失败
        } catch (NamingException e) {
            LogUtil.exception("Can not lookup datasource from JNDI:" + dataSourceName, e);
        }
        return null;
    }


    private synchronized void init() {
        if (ctx != null)
            return;
        try {
            ctx = new InitialContext();
        } catch (NamingException e) {
            throw new IllegalStateException("JNDI init error.", e);
        }
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public Collection<String> getAvailableKeys() {
        if (ctx == null) {
            init();
        }
        List<String> all = new ArrayList<String>();
        try {
            NamingEnumeration<NameClassPair> o = ctx.list("");
            for (; o.hasMore(); ) {
                NameClassPair e = o.next();
                if (isDataSource(e.getClassName())) {
                    all.add(e.getName());
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return all;
    }

    private boolean isDataSource(String className) {
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        if (c == RoutingDataSource.class) {
            return false;
        }
        return DataSource.class.isAssignableFrom(c);
    }

    public String getNamePrifix() {
        return namePrifix;
    }

    public void setNamePrifix(String namePrifix) {
        if (namePrifix != null)
            this.namePrifix = namePrifix;
    }
}
