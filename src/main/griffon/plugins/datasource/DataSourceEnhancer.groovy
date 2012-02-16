/*
 * Copyright 2012 the original author or authors.
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

import griffon.util.CallableWithArgs

/**
 * @author Andres Almiray
 */
final class DataSourceEnhancer {
    private DataSourceEnhancer() {}
    
    static void enhance(MetaClass mc, DataSourceProvider provider = DataSourceHolder.instance) {
        mc.withSql = {Closure closure ->
            provider.withSql('default', closure)
        }
        mc.withSql << {String dataSourceName, Closure closure ->
            provider.withSql(dataSourceName, closure)
        }
        mc.withSql << {CallableWithArgs callable ->
            provider.instance.withSql('default', callable)
        }
        mc.withSql << {String dataSourceName, CallableWithArgs callable ->
            provider.withSql(dataSourceName, callable)
        }
    }
}
