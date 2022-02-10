package io.bcs.fileserver.infrastructure.storage

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.errors.FileStorageException
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
class JdbcFilesystemSpaceManagerITSpec extends Specification {
  private static final String REF_MEDIATYPES_MIGRATION_SCRIPT = "db-init/file/ref.mediatypes.config.changelog.xml"
  private static final String REF_LOCAL_STORAGES_MIGRATION_SCRIPT = "db-init/file/ref.local.storages.config.changelog.xml"
  private static final String BCE_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
  private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()

  @Deployment
  public static Archive "create deployment"() {
    return ArchiveBuilder.jar("jdbc-filesystem-space-manager-spec.jar")
        .resolveDependencies("pom.xml")
        .withScopes(COMPILE, RUNTIME, TEST)
        .resolveDependency("org.liquibase", "liquibase-core")
        .resolveDependency("io.bce", "bce")
        .resolveDependency("io.bce", "bce-test-kit")
        .resolveDependency("io.bce", "bce-spock-ext")
        .apply()
        .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(FilesystemSpaceManager, JdbcFilesystemSpaceManager)
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

  private FilesystemSpaceManager filesystemSpaceManager

  def setup() {
    databaseConfigurer.setup("liquibase/master.changelog.xml")
    databaseConfigurer.setup(REF_MEDIATYPES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(REF_LOCAL_STORAGES_MIGRATION_SCRIPT)
    this.filesystemSpaceManager = new JdbcFilesystemSpaceManager(dataSource)
  }

  def "Scenario: successfully allocate space"() {
    expect: "Filesystem space manager sould allocate requested space"
    filesystemSpaceManager.allocateSpace("application/mediatype", "stored_file", 10) == "LOCAL"
  }

  def "Scenario: space allocation failure"() {
    when: "There isn't applicable local storage for requested allocation"
    filesystemSpaceManager.allocateSpace("application/mediatype", "stored_file", 1000)

    then: "The file storage error should be happened"
    thrown(FileStorageException)
  }

  def "Scenario: successfully release space"() {
    given: "Space is allocated"
    String storageName = filesystemSpaceManager.allocateSpace(
        "application/mediatype", "stored_file", 10)
    
    when: "The space release is requested for it"
    filesystemSpaceManager.releaseSpace(storageName, "stored_file")
    
    then: "No errors should be happened"
    notThrown(Exception)
  }

  def "Scenario: space release failure"() {
    when: "Space release is performed for not allocated space"
    filesystemSpaceManager.releaseSpace("UNKNOWN", "UNKNOWN")
    
    then: "The file storage error should be happened"
    thrown(FileStorageException)    
  }

  def cleanup() {
    databaseConfigurer.tearDown()
  }
}
