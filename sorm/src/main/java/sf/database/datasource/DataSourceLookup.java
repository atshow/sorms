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

package sf.database.datasource;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * Strategy interface for looking up DataSources by name.
 * <p>
 * <p>
 * Used, for example, to resolve data source names in JPA
 * {@code persistence.xml} files.
 * @author Costin Leau
 * @author Juergen Hoeller
 * @see org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager#setDataSourceLookup
 * @since 2.0
 */
public interface DataSourceLookup {

    /**
     * Retrieve the DataSource identified by the given name.
     * @param dataSourceName the name of the DataSource
     * @return the DataSource (never {@code null})
     * @throws DataSourceLookupFailureException if the lookup failed
     */
    DataSource getDataSource(String dataSourceName);

    /**
     * 得到目前可用的key
     * @return
     */
    Collection<String> getAvailableKeys();
}