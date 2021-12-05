package io.bcs.port.adapters.file.validation

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.annotation.Resource
import javax.inject.Inject
import javax.sql.DataSource

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith

import io.bce.MustNeverBeHappenedError
import io.bcs.DictionaryValidation
import io.bcs.DictionaryValidation.DictionaryPredicate
import io.bcs.port.adapters.common.JdbcSequenceGenerator
import io.bcs.port.adapters.file.validation.JdbcMediaTypeAcceptancePredicate
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JdbcMediaTypeAcceptancePredicateITSpec extends Specification {
    private static final String MIGRATION_SCRIPT ="db-init/file/ref.mediatype.changelog.xml"
    private static final String EXISTING_MEDIATYPE = "application/well-mediatype"
    private static final String MISSING_MEDIATYPE = "application/bad-mediatype"
    
    @Deployment
    public static Archive "create deployment"() {
        return ArchiveBuilder.jar("jdbc-media-type-acceptance-predicate-spec.jar")
                .resolveDependencies("pom.xml")
                .withScopes(COMPILE, RUNTIME, TEST)
                .resolveDependency("org.liquibase", "liquibase-core")
                .resolveDependency("org.openclover", "clover")
                .apply()
                .appendPackagesRecursively(MustNeverBeHappenedError.getPackage().getName())
                .appendPackagesRecursively(DatabaseConfigurer.getPackage().getName())
                .appendClasses(JdbcMediaTypeAcceptancePredicate, DictionaryValidation, DictionaryPredicate)
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

    private JdbcMediaTypeAcceptancePredicate predicate
    
    def setup() {
        databaseConfigurer.setup("liquibase/master.changelog.xml")
        databaseConfigurer.setup(MIGRATION_SCRIPT)
        this.predicate = new JdbcMediaTypeAcceptancePredicate(dataSource)
    }

    def "Scenario: check existing mediatype"() {        
        expect: "The predicate should pass existing mediatype"
        predicate.isSatisfiedBy(EXISTING_MEDIATYPE) == true        
    }

    def "Scenario: check missing mediatype"() {
        expect: "The predicate should fail missing mediatype"
        predicate.isSatisfiedBy(MISSING_MEDIATYPE) == false        
    }
    
    def cleanup() {
        databaseConfigurer.tearDown();
    }
}
