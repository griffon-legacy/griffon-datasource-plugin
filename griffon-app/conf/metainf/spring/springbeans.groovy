import griffon.plugins.datasource.DataSourceHolder
import griffon.spring.factory.support.ObjectFactoryBean

beans = {
    'dataSource'(ObjectFactoryBean) {
        object = DataSourceHolder.instance.fetchDataSource('default')
    }
}