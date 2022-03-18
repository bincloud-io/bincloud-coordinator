package io.bcs.fileserver.infrastructure.repositories

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptor
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptorRepository
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JpaLocalStorageDescriptorRepositoryITSpec extends Specification {
  private static final String REF_MEDIATYPES_MIGRATION_SCRIPT ="db-init/file/ref.mediatypes.config.changelog.xml"
  private static final String REF_LOCAL_STORAGES_MIGRATION_SCRIPT = "db-init/file/ref.local.storages.config.changelog.xml"
  private static final String BCS_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
  private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()

  private static final String FILE_STORAGE_NAME = "file.storage.name.12345"
  private static final String FILE_STORAGE_UNKNOWN_NAME = "UNKNOWN"
  private static final String FILE_STORAGE = "LOCAL"
  private static final String MEDIA_TYPE = "application/mediatype"
  private static final String FILE_NAME = "file.txt"
  private static final Long CONTENT_LENGTH = 1000L


  @Deployment
  public static Archive "create deployment"() {
    return ArchiveBuilder.jar("jpa-file-repository-spec.jar")
        .resolveDependencies("pom.xml")
        .withScopes(COMPILE, RUNTIME, TEST)
        .resolveDependency("org.liquibase", "liquibase-core")
        .resolveDependency("io.bce", "bce")
        .resolveDependency("io.bce", "bce-test-kit")
        .resolveDependency("io.bce", "bce-spock-ext")
        .apply()
        .appendPackagesRecursively(BCS_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(JpaStorageDescriptorRepository)
        .appendManifestResource("META-INF/beans.xml", "beans.xml")
        .appendManifestResource("jpa-test/file-mapping-persistence.xml", "persistence.xml")
        .appendManifestResource("META-INF/orm/file-mapping.xml", "orm/file-mapping.xml")
        .appendManifestResource("META-INF/orm/storage-descriptors-mapping.xml", "orm/storage-descriptors-mapping.xml")
        .appendResource("liquibase")
        .appendResource("db-init")
        .build()
  }

  @Inject
  @JdbcLiquibase
  private DatabaseConfigurer databaseConfigurer;

  @PersistenceContext(unitName="central")
  private EntityManager entityManager;

  private StorageDescriptorRepository localStorageDescriptorRepository;

  def setup() {
    databaseConfigurer.setup("liquibase/master.changelog.xml")
    this.localStorageDescriptorRepository = new JpaStorageDescriptorRepository(entityManager)
  }

  def cleanup() {
    databaseConfigurer.tearDown()
  }

  def "Scenario: find existing local storage descriptor by storage file name"() {
    given: "The local file storages are configured"
    databaseConfigurer.setup(REF_MEDIATYPES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(REF_LOCAL_STORAGES_MIGRATION_SCRIPT)

    when: "The registered local storage is requested"
    LocalStorageDescriptor storageDescriptor = localStorageDescriptorRepository.findByName(FILE_STORAGE).get()

    then: "The registered file storage descriptor should be received"
    storageDescriptor.getStorageName() == FILE_STORAGE
    storageDescriptor.getMediaType() == MEDIA_TYPE
    storageDescriptor.getBaseDirectory() == "/srv/bincloud/"
  }
  
  def "Scenario: find missing local storage descriptor by storage file name"() {
    given: "The local file storages are configured"
    databaseConfigurer.setup(REF_MEDIATYPES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(REF_LOCAL_STORAGES_MIGRATION_SCRIPT)

    expect: "File storage for unknown mediatype shouldn't be found"
    localStorageDescriptorRepository.findByName(FILE_STORAGE_UNKNOWN_NAME).isPresent() == false
  }
}
