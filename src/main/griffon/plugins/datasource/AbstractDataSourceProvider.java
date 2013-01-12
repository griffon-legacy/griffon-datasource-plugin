/*
 * Copyright 2012-2013 the original author or authors.
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

package griffon.plugins.datasource;

import griffon.exceptions.GriffonException;
import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import groovy.sql.Sql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractDataSourceProvider implements DataSourceProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDataSourceProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withSql(Closure<R> closure) {
        return withSql(DEFAULT, closure);
    }

    public <R> R withSql(String dataSourceName, Closure<R> closure) {
        if (isBlank(dataSourceName)) dataSourceName = DEFAULT;
        if (closure != null) {
            DataSource ds = getDataSource(dataSourceName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on dataSource '" + dataSourceName + "'");
            }
            Connection connection = null;
            try {
                connection = ds.getConnection();
            } catch (SQLException e) {
                throw new GriffonException(e);
            }
            try {
                return closure.call(dataSourceName, new Sql(connection));
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new GriffonException(e);
                }
            }
        }
        return null;
    }

    public <R> R withSql(CallableWithArgs<R> callable) {
        return withSql(DEFAULT, callable);
    }

    public <R> R withSql(String dataSourceName, CallableWithArgs<R> callable) {
        if (isBlank(dataSourceName)) dataSourceName = DEFAULT;
        if (callable != null) {
            DataSource ds = getDataSource(dataSourceName);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing statement on dataSource '" + dataSourceName + "'");
            }
            Connection connection = null;
            try {
                connection = ds.getConnection();
            } catch (SQLException e) {
                throw new GriffonException(e);
            }
            try {
                callable.setArgs(new Object[]{dataSourceName, new Sql(connection)});
                return callable.call();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new GriffonException(e);
                }
            }
        }
        return null;
    }

    protected abstract DataSource getDataSource(String dataSourceName);
}