package gov.usgs.wma.mlrauthserver.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("default")
@EnableJdbcHttpSession
public class PersistenceConfig {
	@Value("${spring.datasource.url}")
	private String dbConnectionUrl;
	@Value("${spring.datasource.username}")
	private String dbUsername;
	@Value("${spring.datasource.password}")
	private String dbPassword;
	@Value("${spring.session.jdbc.initializer.enabled}")
	private Boolean dbInitializerEnabled;
	
	@Primary
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl(dbConnectionUrl);
		dataSource.setUsername(dbUsername);
		dataSource.setPassword(dbPassword);
		return dataSource;
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource); // <3>
	}
	
	@Bean
	public DataSourceInitializer waterauthSourceInitializer(DataSource dataSource) 
	{
		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(dataSource);
		dataSourceInitializer.setEnabled(dbInitializerEnabled);

		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		dataSourceInitializer.setDatabasePopulator(databasePopulator);

		String sessionSchema = "org/springframework/session/jdbc/schema-mysql.sql";
		String clientSchema = "spring_security_oauth2_mysql.sql";
		databasePopulator.addScript(new ClassPathResource(sessionSchema));
		databasePopulator.addScript(new ClassPathResource(clientSchema));
		
		databasePopulator.setContinueOnError(true);
		
		return dataSourceInitializer;
	}
}