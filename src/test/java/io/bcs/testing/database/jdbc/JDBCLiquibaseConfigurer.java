package io.bcs.testing.database.jdbc;

import javax.sql.DataSource;

import io.bcs.testing.database.LiquibaseDatabaseConfigurer;

public class JDBCLiquibaseConfigurer extends LiquibaseDatabaseConfigurer {
	public JDBCLiquibaseConfigurer(final DataSource dataSource) {
		super(new JDBCLiquibaseConnector(dataSource));
	}
}
