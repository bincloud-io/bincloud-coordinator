package io.bcs.port.adapters.generators

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

import io.bce.Generator
import io.bce.MustNeverBeHappenedError
import io.bce.domain.errors.ApplicationException
import io.bcs.port.adapters.generators.JDBCSequenceGenerator
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JDBCSequenceGeneratorITSpec extends Specification {
    private static final String MIGRATION_SCRIPT ="db-init/sequence/sequence.changelog.xml"
    private static final String IN_MEM_DATABASE_URL = "jdbc:h2:mem:sequences"
    private static final String DATABASE_USERNAME = "sa"
    private static final String DATABASE_PASSWORD = ""

    @Deployment
    public static Archive "create deployment"() {
        return ArchiveBuilder.jar("jdbc-sequence-generator-spec.jar")
                .resolveDependencies("pom.xml")
                .withScopes(COMPILE, RUNTIME, TEST)
                .resolveDependency("org.liquibase", "liquibase-core")
                .resolveDependency("org.openclover", "clover")
                .apply()
                .appendPackagesRecursively(MustNeverBeHappenedError.getPackage().getName())
                .appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
                .appendClasses(JDBCSequenceGenerator)
                .appendResource("liquibase")
                .appendResource("db-init")
                .appendManifestResource("META-INF/beans.xml", "beans.xml")
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
        databaseConfigurer.setup(MIGRATION_SCRIPT)

        and: "The sequence generator connected to its one"
        Generator<Long> generator = new JDBCSequenceGenerator(dataSource, "SEQUENCE")

        expect: "The auto incremented integer numbers will be generated"
        IntStream.range(1, 10).forEach({
            generator.generateNext() == it
        })
    }

    def cleanup() {
        databaseConfigurer.tearDown();
    }
}
