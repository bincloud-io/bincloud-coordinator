package io.bcs.testing.database.jdbc;

import java.util.function.Supplier;

import javax.sql.DataSource;

import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JDBCLiquibaseConnector implements Supplier<DatabaseConnection> {
	private final DataSource dataSource;
	
	@Override
	@SneakyThrows
	public DatabaseConnection get() {
		JdbcConnection connection = new JdbcConnection(dataSource.getConnection());
		connection.setAutoCommit(true);
		return connection;
	}
}
