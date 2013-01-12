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

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;

/**
 * @author Andres Almiray
 */
public class DataSourceContributionAdapter implements DataSourceContributionHandler {
    private static final String DEFAULT = "default";

    private DataSourceProvider provider = DefaultDataSourceProvider.getInstance();

    public void setDataSourceProvider(DataSourceProvider provider) {
        this.provider = provider != null ? provider : DefaultDataSourceProvider.getInstance();
    }

    public DataSourceProvider getDataSourceProvider() {
        return provider;
    }

    public <R> R withSql(Closure<R> closure) {
        return withSql(DEFAULT, closure);
    }

    public <R> R withSql(String dataSourceName, Closure<R> closure) {
        return provider.withSql(dataSourceName, closure);
    }

    public <R> R withSql(CallableWithArgs<R> callable) {
        return withSql(DEFAULT, callable);
    }

    public <R> R withSql(String dataSourceName, CallableWithArgs<R> callable) {
        return provider.withSql(dataSourceName, callable);
    }
}