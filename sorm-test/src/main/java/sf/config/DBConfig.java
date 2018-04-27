package sf.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.zaxxer.hikari.HikariDataSource;
import org.smallframework.spring.DaoTemplate;
import org.smallframework.spring.OrmConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import sf.database.datasource.RoutingDataSource;
import sf.tools.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
// @EnableConfigurationProperties(GeeQueryConfigProperties.class)
public class DBConfig implements EnvironmentAware {
	private Environment env;

	@Override
	public void setEnvironment(Environment env) {
		this.env = env;
	}

//	@Bean(destroyMethod = "close", initMethod = "init")


//	@Primary
//	@Bean(name="mysql")
//	@ConfigurationProperties("spring.datasource.druid.one")
	public DataSource dataSourceMysql(){
		DruidDataSource dds= DruidDataSourceBuilder.create().build();
		return dds;
	}

	@Bean(name="sqlite")
	@ConfigurationProperties("spring.datasource.druid.two")
	public DataSource dataSourceSqlite(){
//		return DruidDataSourceBuilder.create().build();
		return DataSourceBuilder.create().type(HikariDataSource.class).build();
	}

    @Primary
	@Bean(name = "xaMysql")
//    @ConfigurationProperties(prefix = "spring.jta.atomikos.datasource.jta-income")
    public DataSource dataSourceJTAMyql() {
        return new AtomikosDataSourceBean();
    }

//	@Bean
	public OrmConfig getOrmConfig(DaoTemplate dt) {
		OrmConfig config = new OrmConfig();
		config.setPackagesToScan(StringUtils.split("db.domain",","));
		config.setDbClient(dt);
		config.setUseTail(true);
		config.setFastBeanMethod(false);
		config.init();
		return config;
	}

	@Bean(name="jdbcTemplate")
	public JdbcTemplate geJdbcTemplate(DataSource ds) {
		JdbcTemplate jt = new JdbcTemplate(ds);
		return jt;
	}
	
	@Bean(name="daoTemplate")
	public DaoTemplate geDaoTemplate(@Qualifier("xaMysql")DataSource xaMysql,@Qualifier("sqlite")DataSource sqlite) {
		RoutingDataSource routing = new RoutingDataSource();
		Map<String, Object> targetDataSources = new HashMap<>();
		targetDataSources.put("mysql", xaMysql);
		targetDataSources.put("sqlite", sqlite);
		routing.setTargetDataSources(targetDataSources);
		routing.setDefaultTargetDataSource("mysql");
		DaoTemplate dt = new DaoTemplate(routing);
//		DaoTemplate dt = new DaoTemplate(xaMysql);
		return dt;
	}


	///以下为jooq配置
}
