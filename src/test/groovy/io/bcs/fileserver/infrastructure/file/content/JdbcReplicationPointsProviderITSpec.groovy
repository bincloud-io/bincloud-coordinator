package io.bcs.fileserver.infrastructure.file.content

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler.DistribuionPointsProvider
import io.bcs.fileserver.infrastructure.file.JdbcReplicationPointsProvider
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import javax.annotation.Resource
import javax.inject.Inject
import javax.sql.DataSource
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JdbcReplicationPointsProviderITSpec extends Specification {
  private static final String REF_DISTRIBUTION_POINTS_MIGRATION_SCRIPT = "db-init/file/distribution.points.changelog.xml"
  private static final String BCE_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
  private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()
  private static final String DISTRIBUTION_POINT_1 = "DP_1"
  private static final String DISTRIBUTION_POINT_2 = "DP_2"

  @Deployment
  public static Archive "create deployment"() {
    return ArchiveBuilder.jar("jdbc-replication-points-provider-spec.jar")
        .resolveDependencies("pom.xml")
        .withScopes(COMPILE, RUNTIME, TEST)
        .resolveDependency("org.liquibase", "liquibase-core")
        .resolveDependency("io.bce", "bce")
        .resolveDependency("io.bce", "bce-test-kit")
        .resolveDependency("io.bce", "bce-spock-ext")
        .apply()
        .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(CreatedFileSynchronizationHandler, JdbcReplicationPointsProvider)
        .appendManifestResource("META-INF/beans.xml", "beans.xml")
        .appendResource("liquibase")
        .appendResource("db-init")
        .build()
  }
  
  @Inject
  @JdbcLiquibase
  private DatabaseConfigurer databaseConfigurer

  @Resource(lookup="java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;
  
  private DistribuionPointsProvider distribuionPointsProvider
  
  def setup() {
    databaseConfigurer.setup("liquibase/master.changelog.xml")
    databaseConfigurer.setup(REF_DISTRIBUTION_POINTS_MIGRATION_SCRIPT)
    this.distribuionPointsProvider = new JdbcReplicationPointsProvider(dataSource)
  }
  
  def "Scenario: provide all distribution points"() {
    expect: "All distribution points should be obtained"
    Collection<String> distributionPoints = distribuionPointsProvider.findDistributionPoints()
    distributionPoints.contains(DISTRIBUTION_POINT_1)
    distributionPoints.contains(DISTRIBUTION_POINT_2)
    distributionPoints.size() == 2
  }
  
  def cleanup() {
    databaseConfigurer.tearDown()
  }
}
