package org.smallframework.springboot;

import org.smallframework.spring.DaoTemplate;
import org.smallframework.spring.OrmConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sf.database.dao.DBClient;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@ConditionalOnClass({DBClient.class})
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(SmallOrmProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class SmallOrmAutoConfiguration {
    @Resource
    private SmallOrmProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public OrmConfig ormConfig(DaoTemplate dt) throws Exception {
        OrmConfig config = new OrmConfig();
        config.setDbClient(dt);
        config.setFastBeanMethod(properties.isFastBeanMethod());
        config.setPackagesToScan(properties.getPackages().split("\\,"));
        config.setUseTail(properties.isUseTail());
        config.setAllowDropColumn(properties.isAllowDropColumn());
        config.setCreateTable(properties.isCreateTable());
        config.setShowSql(properties.isShowSql());
        config.setSqlTemplate(properties.getSqlTemplate());
        config.init();
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public DaoTemplate daoTemplate(DataSource dataSource) {
        DaoTemplate dt = new DaoTemplate(dataSource);
        return dt;
    }
}
