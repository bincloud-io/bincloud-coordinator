package io.bcs.testing.database.jdbc.cdi;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

import io.bcs.testing.database.DatabaseConfigurer;
import io.bcs.testing.database.jdbc.JDBCLiquibaseConfigurer;
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
