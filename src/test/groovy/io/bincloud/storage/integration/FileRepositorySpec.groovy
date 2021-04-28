package io.bincloud.storage.integration

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.inject.Inject

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.storage.port.adapter.file.JPAFileRepository
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)

class FileRepositorySpec extends Specification {
	@Deployment
	public static Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
			.appendPackagesRecursively(File.getPackage().getName())
			.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
			.appendClasses(JPAFileRepository)
			.appendManifestResource("META-INF/beans.xml", "beans.xml")
			.appendManifestResource("META-INF/persistence.xml", "persistence.xml")
			.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("org.liquibase", "liquibase-core")
				.apply()
		.build()
	}
	
	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;
	
	def "Scenario: database configurer injected"() {
		expect:
		databaseConfigurer != null
	}
}
