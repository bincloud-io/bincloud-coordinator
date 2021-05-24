package io.bincloud.common.port.adapters.generators

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import java.util.stream.IntStream

import javax.annotation.Resource
import javax.inject.Inject
import javax.sql.DataSource

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bincloud.common.domain.model.error.ApplicationException
import io.bincloud.common.domain.model.generator.SequentialGenerator
import io.bincloud.testing.archive.ArchiveBuilder
import io.bincloud.testing.database.DatabaseConfigurer
import io.bincloud.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Narrative
import spock.lang.Specification

@Narrative("""
	To have possibility generate unique sequential auto incremented numbers,
	as a developer I am needed in a component which will generate them and 
	give warranty that each of them is unique value 
""")
@RunWith(ArquillianSputnik)
class JDBCSequenceGeneratorITSpec extends Specification {
	@Deployment
	public static Archive "create deployment"() {
		return ArchiveBuilder.jar("file-repository-spec.jar")
				.appendPackagesRecursively(File.getPackage().getName())
				.appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
				.appendPackagesRecursively(ApplicationException.getPackage().getName())
				.appendClasses(SequentialGenerator, JDBCSequenceGenerator)
				.appendResource("liquibase")
				.appendResource("liquibase-test")
				.appendManifestResource("META-INF/beans.xml", "beans.xml")
				.resolveDependencies("pom.xml")
				.withScopes(COMPILE, RUNTIME, TEST)
				.resolveDependency("org.liquibase", "liquibase-core")
				.apply()
				.build()
	}

	@Inject
	@JdbcLiquibase
	private DatabaseConfigurer databaseConfigurer;

	@Resource(lookup="java:/jdbc/BC_CENTRAL")
	private DataSource dataSource;

	def setup() {
		databaseConfigurer.setup("liquibase/master.changelog.xml")
	}

	def "Scenario: generate sequence of numbers"() {
		given: "The prepared database with registered sequence"
		databaseConfigurer.setup("liquibase-test/common/JDBCSequenceGeneratorSpec.changelog.xml");

		and: "The sequence generator connected to its one"
		SequentialGenerator<Long> generator = new JDBCSequenceGenerator(dataSource, "SEQUENCE")

		expect: "The auto incremented integer numbers will be generated"
		IntStream.range(1, 10).forEach({
			generator.nextValue() == it
		})
	}

	def cleanup() {
		databaseConfigurer.tearDown();
	}
}
