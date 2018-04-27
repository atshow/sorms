package org.smallframework.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class SessionFactoryBean extends OrmConfig implements FactoryBean<OrmConfig>, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        init();
    }

    @Override
    public OrmConfig getObject() throws Exception {
        // TODO Auto-generated method stub
        return new OrmConfig();
    }

    @Override
    public Class<?> getObjectType() {
        // TODO Auto-generated method stub
        return OrmConfig.class;
    }

    @Override
    public boolean isSingleton() {
        // TODO Auto-generated method stub
        return false;
    }

}
