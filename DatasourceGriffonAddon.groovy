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

import griffon.core.GriffonClass
import griffon.core.GriffonApplication
import griffon.plugins.datasource.DataSourceHolder
import griffon.plugins.datasource.DataSourceEnhancer
import griffon.plugins.datasource.DataSourceContributionHandler

/**
 * @author Andres Almiray
 */
class DatasourceGriffonAddon {
    void addonPostInit(GriffonApplication app) {
        def types = app.config.griffon?.datasource?.injectInto ?: ['controller']
        for(String type : types) {
            for(GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                if (DataSourceContributionHandler.isAssignableFrom(gc.clazz)) continue
                DataSourceEnhancer.enhance(gc.metaClass)
            }
        }
    }

    Map events = [
        BeforeWeld: { beans ->
            beans.dataSource = DataSourceHolder.instance.getDataSource('default')
        }
    ]

    def exportWithJmx = { exporter, domain, ctx ->
        exporter.beans."${domain}:service=datasource,type=configuration" = DataSourceHolder.instance.getDataSource('default')
    }
}
