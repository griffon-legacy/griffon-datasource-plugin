/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
class DatasourceGriffonPlugin {
    // the plugin version
    String version = '0.4'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-datasource-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'Base datasource support'
    String description = '''
The DataSource plugin enables lightweight access multiple SQL datasources.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * DataSource.groovy - contains the dataSource definitions.

A new dynamic method named `withSql` will be injected into all controllers,
giving you access to a `groovy.sql.Sql` object, with which you'll be able
to make calls to the dataSource. Remember to make all dataSource calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple dataSources. If no dataSourceName is specified when calling
it then the default dataSource will be selected. Here are two example usages, the first
queries against the default dataSource while the second queries a dataSource whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllDataSources = {
            withSql { dataSourceName, sql -> ... }
            withSql('internal') { dataSourceName, sql -> ... }
        }
    }
    
This method is also accessible to any component through the singleton `griffon.plugins.datasource.DataSourceConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`DataSourceEnhancer.enhance(metaClassInstance, datasourceProviderInstance)`.

### JMX support

This plugin exposes the default dataSource if the [JMX plugin][1] is installed. The name used is `:service=datasource`.

### Spring Support

The default dataSource bean is available under the name `dataSource` if the [Spring plugin][2] is installed.

### Weld Support

The default dataSource bean is available under the name `dataSource` if the [Weld plugin][3] is installed.

Configuration
-------------
### Dynamic method injection

The `withSql()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.datasource.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * DataSourceConnectStart[config, dataSourceName] - triggered before connecting to the dataSource
 * DataSourceConnectEnd[dataSourceName, dataSource] - triggered after connecting to the dataSource
 * DataSourceDisconnectStart[config, dataSourceName, dataSource] - triggered before disconnecting from the dataSource
 * DataSourceDisconnectEnd[config, dataSourceName] - triggered after disconnecting from the dataSource

### Multiple DataSources

The config file `DataSource.groovy` defines a default dataSource block. As the name
implies this is the dataSource used by default, however you can configure named dataSources
by adding a new config block. For example connecting to a dataSource whose name is 'internal'
can be done in this way

    dataSources {
        internal {
            driverClassName = 'org.h2.Driver'
            username = 'sa'
            password = ''
            tokenizeddl = false // set this to true if using MySQL or any other
                                // RDBMS that requires execution of DDL statements
                                // on separate calls
            pool {
                maxWait = 60000
                maxIdle = 5
                maxActive = 8
            }
        }
    }

This block can be used inside the `environments()` block in the same way as the
default dataSource block is used.

Testing
-------
The `withSql()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `DataSourceEnhancer.enhance(metaClassInstance, datasourceProviderInstance)` where 
`datasourceProviderInstance` is of type `griffon.plugins.datasource.DataSourceProvider`. The contract for this interface looks like this

    public interface DataSourceProvider {
        Object withSql(Closure closure);
        Object withSql(String dataSourceName, Closure closure);
        <T> T withSql(CallableWithArgs<T> callable);
        <T> T withSql(String dataSourceName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyDataSourceProvider implements DataSourceProvider {
        Object withSql(String dataSourceName = 'default', Closure closure) { null }
        public <T> T withSql(String dataSourceName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            DataSourceEnhancer.enhance(service.metaClass, new MyDataSourceProvider())
            // exercise service methods
        }
    }


[1]: /plugin/jmx
[2]: /plugin/spring
[3]: /plugin/weld
'''
}
