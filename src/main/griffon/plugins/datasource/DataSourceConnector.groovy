/*
 * Copyright 2009-2013 the original author or authors.
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
package griffon.plugins.datasource

import javax.sql.DataSource

import org.apache.commons.pool.ObjectPool
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.commons.dbcp.ConnectionFactory
import org.apache.commons.dbcp.PoolingDataSource
import org.apache.commons.dbcp.PoolableConnectionFactory
import org.apache.commons.dbcp.DriverManagerConnectionFactory

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
class DataSourceConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceConnector)

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.datasource) {
            app.config.pluginConfig.datasource = ConfigUtils.loadConfigWithI18n('DataSource')
        }
        app.config.pluginConfig.datasource
    }

    private ConfigObject narrowConfig(ConfigObject config, String dataSourceName) {
        if (config.containsKey('dataSource') && dataSourceName == DEFAULT) {
            return config.dataSource
        } else if (config.containsKey('dataSources')) {
            return config.dataSources[dataSourceName]
        }
        return config
    }

    DataSource connect(GriffonApplication app, ConfigObject config, String dataSourceName = DEFAULT) {
        if (DataSourceHolder.instance.isDataSourceConnected(dataSourceName)) {
            return DataSourceHolder.instance.getDataSource(dataSourceName)
        }

        config = narrowConfig(config, dataSourceName)
        app.event('DataSourceConnectStart', [config, dataSourceName])
        DataSource ds = createDataSource(config, dataSourceName)
        DataSourceHolder.instance.setDataSource(dataSourceName, ds)
        def skipSchema = config.schema?.skip ?: false
        if (!skipSchema) createSchema(app, config, dataSourceName)
        app.event('DataSourceConnectEnd', [dataSourceName, ds])
        ds
    }

    void disconnect(GriffonApplication app, ConfigObject config, String dataSourceName = DEFAULT) {
        if (DataSourceHolder.instance.isDataSourceConnected(dataSourceName)) {
            config = narrowConfig(config, dataSourceName)
            app.event('DataSourceDisconnectStart', [config, dataSourceName, DataSourceHolder.instance.getDataSource(dataSourceName)])
            app.event('DataSourceDisconnectEnd', [config, dataSourceName])
            DataSourceHolder.instance.disconnectDataSource(dataSourceName)
        }
    }

    private DataSource createDataSource(ConfigObject config, String dataSourceName) {
        Class.forName(config.driverClassName.toString())
        ObjectPool connectionPool = new GenericObjectPool(null)
        if (config.pool) {
            if (config.pool.maxWait != null) connectionPool.maxWait = config.pool.maxWait
            if (config.pool.maxIdle != null) connectionPool.maxIdle = config.pool.maxIdle
            if (config.pool.maxActive != null) connectionPool.maxActive = config.pool.maxActive
        }

        String url = config.url.toString()
        String username = config.username.toString()
        String password = config.password.toString()
        ConnectionFactory connectionFactory = null
        if (username) {
            connectionFactory = new DriverManagerConnectionFactory(url, username, password)
        } else {
            connectionFactory = new DriverManagerConnectionFactory(url, null)
        }
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true)
        new PoolingDataSource(connectionPool)
    }

    private void createSchema(GriffonApplication app, ConfigObject config, String dataSourceName) {
        String dbCreate = config.dbCreate.toString()
        if (dbCreate != 'create') return

        String env = getEnvironmentShortName()
        URL ddl = null
        for(String schemaName : [dataSourceName + '-schema-'+ env +'.ddl', dataSourceName + '-schema.ddl', 'schema-'+ env +'.ddl', 'schema.ddl']) {
            ddl = getClass().classLoader.getResource(schemaName)
            if (!ddl) {
                LOG.warn("DataSource[${dataSourceName}].dbCreate was set to 'create' but ${schemaName} was not found in classpath.")
            } else {
                break
            }
        }
        if (!ddl) {
            LOG.error("DataSource[${dataSourceName}].dbCreate was set to 'create' but no suitable schema was found in classpath.")
            return
        }

        boolean tokenizeddl = config.tokenizeddl ?: false
        resolveDataSourceProvider(app).withSql(dataSourceName) { dsName, sql ->
            if (!tokenizeddl) {
                sql.execute(ddl.text)
            } else {
                ddl.text.split(';').each { stmnt ->
                    if (stmnt?.trim()) sql.execute(stmnt + ';')
                }
            }
        }
    }

    DataSourceProvider resolveDataSourceProvider(GriffonApplication app) {
        def dataSourceProvider = app.config.dataSourceProvider
        if (dataSourceProvider instanceof Class) {
            dataSourceProvider = dataSourceProvider.newInstance()
            app.config.dataSourceProvider = dataSourceProvider
        } else if (!dataSourceProvider) {
            dataSourceProvider = DefaultDataSourceProvider.instance
            app.config.dataSourceProvider = dataSourceProvider
        }
        dataSourceProvider
    }

    private String getEnvironmentShortName() {
        switch(Environment.current) {
            case Environment.DEVELOPMENT: return 'dev'
            case Environment.TEST:        return 'test'
            case Environment.PRODUCTION:  return 'prod'
            default: return Environment.current.name
        }
    }
}