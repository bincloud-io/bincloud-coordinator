package io.bincloud.testing.database.jdbc;

import javax.sql.DataSource;

import io.bincloud.testing.database.LiquibaseDatabaseConfigurer;

public class JDBCLiquibaseConfigurer extends LiquibaseDatabaseConfigurer {
	public JDBCLiquibaseConfigurer(final DataSource dataSource) {
		super(new JDBCLiquibaseConnector(dataSource));
	}
}
