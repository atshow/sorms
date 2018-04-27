/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sf.database.datasource.lookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sf.common.Entry;
import sf.database.datasource.AbstractDataSource;
import sf.database.datasource.DataSourceLookup;
import sf.database.datasource.IRoutingDataSource;
import sf.tools.utils.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract {@link javax.sql.DataSource} implementation that routes {@link #getConnection()}
 * calls to one of various target DataSources based on a lookup key. The latter is usually
 * (but not necessarily) determined through some thread-bound transaction context.
 * @author Juergen Hoeller
 * @see #setTargetDataSources
 * @see #setDefaultTargetDataSource
 * @see #determineCurrentLookupKey()
 * @since 2.0.1
 */
public abstract class AbstractRoutingDataSource extends AbstractDataSource implements IRoutingDataSource {

    private static Logger logger = LoggerFactory.getLogger(AbstractRoutingDataSource.class);
    /**
     * 传入的目标数据源(必传)
     */
    private Map<String, Object> targetDataSources;

    //默认的数据源(必传)
    protected String defaultTargetDataSource;
    //查找器
    protected DataSourceLookup dataSourceLookup = new MapDataSourceLookup();
    //缓存已经查找到结果
    protected Map<String, DataSource> resolvedDataSources = new ConcurrentHashMap<>();
    //记录使用过的第一个数据源作为缺省的
    protected Map.Entry<String, DataSource> resolvedDefaultDataSource;

    //记录当前datasource的状态，用于确定本次操作应该返回哪个数据源的连接
    private final ThreadLocal<String> keys = new ThreadLocal<String>();

    Lock lock = new ReentrantLock();

    /**
     * Specify the map of target DataSources, with the lookup key as key.
     * The mapped value can either be a corresponding {@link javax.sql.DataSource}
     * instance or a data source name String (to be resolved via a
     * {@link #setDataSourceLookup DataSourceLookup}).
     * <p>The key can be of arbitrary type; this class implements the
     * generic lookup process only. The concrete key representation will
     * be handled by {@link #resolveSpecifiedLookupKey(String)} and
     * {@link #determineCurrentLookupKey()}.
     */
    public void setTargetDataSources(Map<String, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    /**
     * Specify the default target DataSource, if any.
     * <p>The mapped value can either be a corresponding {@link javax.sql.DataSource}
     * instance or a data source name String (to be resolved via a
     * {@link #setDataSourceLookup DataSourceLookup}).
     * <p>This DataSource will be used as target if none of the keyed
     * {@link #setTargetDataSources targetDataSources} match the
     * {@link #determineCurrentLookupKey()} current lookup key.
     */
    public void setDefaultTargetDataSource(String defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    /**
     * Set the DataSourceLookup implementation to use for resolving data source
     * name Strings in the {@link #setTargetDataSources targetDataSources} map.
     * <p>Default is a {@link JndiDataSourceLookup}, allowing the JNDI names
     * of application server DataSources to be specified directly.
     */
    public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
        this.dataSourceLookup = dataSourceLookup;
    }


    /**
     * Resolve the given lookup key object, as specified in the
     * {@link #setTargetDataSources targetDataSources} map, into
     * the actual lookup key to be used for matching with the
     * {@link #determineCurrentLookupKey() current lookup key}.
     * <p>The default implementation simply returns the given key as-is.
     * @param lookupKey the lookup key object as specified by the user
     * @return the lookup key as needed for matching
     */
    protected String resolveSpecifiedLookupKey(String lookupKey) {
        return lookupKey;
    }

    /**
     * Resolve the specified data source object into a DataSource instance.
     * <p>The default implementation handles DataSource instances and data source
     * names (to be resolved via a {@link #setDataSourceLookup DataSourceLookup}).
     * @param dataSource the data source value object as specified in the
     *                   {@link #setTargetDataSources targetDataSources} map
     * @return the resolved DataSource (never {@code null})
     * @throws IllegalArgumentException in case of an unsupported value type
     */
    protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
        if (dataSource instanceof DataSource) {
            return (DataSource) dataSource;
        } else if (dataSource instanceof String) {
            DataSource ds = resolvedDataSources.get(dataSource);
            if (ds == null) {
                ds = this.dataSourceLookup.getDataSource((String) dataSource);
            }
            return ds;
        } else {
            throw new IllegalArgumentException(
                    "Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
        }
    }


    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    /**
     * Retrieve the current target DataSource. Determines the
     * {@link #determineCurrentLookupKey() current lookup key}, performs
     * a lookup in the {@link #setTargetDataSources targetDataSources} map,
     * falls back to the specified
     * {@link #setDefaultTargetDataSource default target DataSource} if necessary.
     * @see #determineCurrentLookupKey()
     */
    protected DataSource determineTargetDataSource() {
        String lookupKey = determineCurrentLookupKey();
        if (lookupKey == null) {
            Map.Entry<String, DataSource> result = getDefaultDataSource();
            if (result == null)
                throw new IllegalArgumentException("Can not determine default datasource. avaliable datasoruces are:" + getDataSourceNames());
            return result.getValue();
        } else {
            return getDataSource(lookupKey);
        }
    }

    /**
     * 获取数据源的键
     * Determine the current lookup key. This will typically be
     * implemented to check a thread-bound transaction context.
     * <p>Allows for arbitrary keys. The returned key needs
     * to match the stored lookup key type, as resolved by the
     * {@link #resolveSpecifiedLookupKey} method.
     */
    protected String determineCurrentLookupKey() {
        return keys.get();
    }


    //////////// 以下为扩展接口.
    @Override
    protected Class<? extends DataSource> getWrappedClass() {
        return null;
    }

    @Override
    public boolean isSingleDatasource() {
        return targetDataSources.size() == 1;
    }

    @Override
    public Set<String> getDataSourceNames() {
        Set<String> set = new HashSet<String>();
        for (Object obj : resolvedDataSources.keySet()) {
            set.add(obj.toString());
        }
        if (dataSourceLookup != null)
            set.addAll(dataSourceLookup.getAvailableKeys());
        return set;
    }


    @Override
    public DataSource getDataSource(String lookupKey) throws NoSuchElementException {
        DataSource dataSource = resolvedDataSources.get(lookupKey);
        if (dataSource == null && lookupKey == null) {
            throw new IllegalArgumentException("Can not lookup by empty Key");//不允许这样使用，这样做会造成上层无法得到default的key,从而会将null ,"" ,"DEFAULT"这种表示误认为是三个数据源，其实是同一个。
        }
        if (dataSource == null) {
            dataSource = lookup(lookupKey);
        }
        if (dataSource == null) {
            throw new NoSuchElementException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    @Override
    public Map.Entry<String, DataSource> getDefaultDataSource() {
        if (resolvedDefaultDataSource != null) { //如果缺省数据源已经计算出来，那么直接返回。
            return resolvedDefaultDataSource;
        }
        DataSource ds = null;
        if (dataSourceLookup != null) {
            String defaultKey = defaultTargetDataSource;        //计算缺省数据源
            if (defaultKey != null) {
                logger.info("Lookup key is null, using the default datasource: {}", defaultKey);
                ds = resolvedDataSources.get(defaultKey);
                if (ds == null) {
                    ds = lookup(defaultKey);
                }
                if (ds == null) {
                    throw new NullPointerException("The default datasource '" + defaultKey + "' is not exist, please check you configuration!");
                }
                resolvedDefaultDataSource = new sf.common.Entry<>(defaultKey, ds); //记录缺省数据源
                return resolvedDefaultDataSource;
            }
        }
        //无法计算和找到缺省数据源，将之前首次使用的数据源当作缺省数据源返回（可能为null）
        return null;
    }

    //去找寻数据源配置
    private DataSource lookup(String lookupKey) {
        Assert.notNull(lookupKey, "");//不允许ket为空的查找
        DataSource ds = null;
        if (resolvedDataSources.isEmpty()) {
            lock.lock();
            afterPropertiesSet();
            lock.unlock();
        }
        if (!resolvedDataSources.isEmpty()) {
            ds = resolvedDataSources.get(lookupKey);
            if (ds != null) ds = checkDatasource(ds);
        }
        return ds;
    }

    public void afterPropertiesSet() {
        if (this.targetDataSources == null) {
            throw new IllegalArgumentException("Property 'targetDataSources' is required");
        }
        this.resolvedDataSources = new HashMap<String, DataSource>(this.targetDataSources.size());
        for (Map.Entry<String, Object> entry : this.targetDataSources.entrySet()) {
            String lookupKey = resolveSpecifiedLookupKey(entry.getKey());
            DataSource dataSource = resolveSpecifiedDataSource(entry.getValue());
            this.resolvedDataSources.put(lookupKey, dataSource);
        }
        if (this.defaultTargetDataSource != null) {
            this.resolvedDefaultDataSource = new Entry<>(defaultTargetDataSource, resolveSpecifiedDataSource(this.defaultTargetDataSource));
        }
    }

    /**
     * 供子类覆盖，用于挑选lookup返回的datasource是否合理。（检查实例，池，XA等特性）
     * <p>
     * 有三种行为可供使用
     * 1、返回再行包装后的Datasource
     * 2、返回Null，本次lookup作废，会尝试去用DataSourceInfo查找。
     * 3、直接抛出异常，提示用户配置错误等.
     * @param ds
     * @return
     */
    protected DataSource checkDatasource(DataSource ds) {
        return ds;
    }

    /**
     * 设置查找的数据源键
     * @param key
     */
    protected void setLookupKey(String key) {
        keys.set(key);
    }
}
