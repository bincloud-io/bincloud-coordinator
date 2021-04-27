package io.bincloud.testing.database;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class LiquibaseDatabaseConfigurer implements DatabaseConfigurer {
	private final Supplier<DatabaseConnection> liquibaseConnector;
	private final List<String> changesetsStack = new LinkedList<String>();
	
	@Override
	public void setup(String configLocation) {
		updateDatabase(configLocation);
		cacheConfigPath(configLocation);
	}

	@Override
	public void tearDown() {
		this.changesetsStack.forEach(changeset -> rollbackConfig(changeset));
		this.changesetsStack.clear();
	}
	
	private void cacheConfigPath(String configLocation) {
		this.changesetsStack.add(0, configLocation);
	}
	
	@SneakyThrows
	private void updateDatabase(String configLocation) {
		createLiquibaseFor(configLocation).update(new Contexts());
	}
	
	@SneakyThrows
	private void rollbackConfig(String configLocation) {
		Liquibase liquibase = createLiquibaseFor(configLocation);
		liquibase.rollback(getChangingsCount(liquibase), new Contexts(), new LabelExpression());
	}
	
	@SneakyThrows
	private int getChangingsCount(Liquibase liquibase) {
		return liquibase.getDatabaseChangeLog().getChangeSets().size();
	}
	
	@SneakyThrows
	private Liquibase createLiquibaseFor(String configLocation) {
		DatabaseConnection databaseConnection = liquibaseConnector.get();
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
		return new Liquibase(configLocation, new ClassLoaderResourceAccessor(), database);
	}
}
