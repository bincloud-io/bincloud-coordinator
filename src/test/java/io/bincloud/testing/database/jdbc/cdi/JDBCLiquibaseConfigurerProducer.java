package io.bincloud.testing.database.jdbc.cdi;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import io.bincloud.testing.database.DatabaseConfigurer;
import io.bincloud.testing.database.jdbc.JDBCLiquibaseConfigurer;
import lombok.NoArgsConstructor;

@ApplicationScoped
@NoArgsConstructor
public class JDBCLiquibaseConfigurerProducer {
	@Resource(lookup = "java:/jdbc/BC_CENTRAL")
	private DataSource dataSource;
	
	
	@Produces
	@JdbcLiquibase
	public DatabaseConfigurer databaseConfigurer() {
		return new JDBCLiquibaseConfigurer(dataSource);
	}
}
