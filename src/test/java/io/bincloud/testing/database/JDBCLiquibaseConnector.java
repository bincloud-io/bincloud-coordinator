package io.bincloud.testing.database;

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
		return new JdbcConnection(dataSource.getConnection());
	}
}
