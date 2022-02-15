package io.bcs.fileserver.infrastructure.repositories

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.model.file.metadata.Disposition
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata
import io.bcs.fileserver.domain.model.file.metadata.FileMetadataRepository
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JpaFileMetadataRepositoryITSpec extends Specification {
  private static final String REF_MEDIATYPES_MIGRATION_SCRIPT ="db-init/file/ref.mediatypes.config.changelog.xml"
  private static final String REF_LOCAL_STORAGES_MIGRATION_SCRIPT = "db-init/file/ref.local.storages.config.changelog.xml"
  private static final String LOCAL_FILES_METADATA_MIGRATION_SCRIPT = "db-init/file/file.metadata.changelog.xml"
  private static final String BCE_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
  private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()

  private static final String FILE_STORAGE_NAME = "e95b72b3-54dd-11ec-8d39-0242ac130002"
  private static final String MEDIA_TYPE = "application/mediatype"
  private static final String FILE_NAME = "file.txt"
  private static final Long CONTENT_LENGTH = 1000L


  @Deployment
  public static Archive "create deployment"() {
    return ArchiveBuilder.jar("jpa-file-metadata-repository-spec.jar")
        .resolveDependencies("pom.xml")
        .withScopes(COMPILE, RUNTIME, TEST)
        .resolveDependency("org.liquibase", "liquibase-core")
        .resolveDependency("io.bce", "bce")
        .resolveDependency("io.bce", "bce-test-kit")
        .resolveDependency("io.bce", "bce-spock-ext")
        .apply()
        .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(JpaFileMetadataRepository)
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

  @Inject
  private TransactionManager transactionManager;

  private FileMetadataRepository fileMetadataRepository

  def setup() {
    databaseConfigurer.setup("liquibase/master.changelog.xml")
    this.fileMetadataRepository = new JpaFileMetadataRepository(entityManager)
  }



  def "Scenario: retrieve file metadata by id"() {
    given: "The file metadata is stored into repository"
    databaseConfigurer.setup(REF_MEDIATYPES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(REF_LOCAL_STORAGES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(LOCAL_FILES_METADATA_MIGRATION_SCRIPT)

    when: "The file metadata is received by id"
    FileMetadata fileMetadata = fileMetadataRepository.findById(FILE_STORAGE_NAME).get()

    then: "The existing metadata should be received"
    fileMetadata.getFileName() == FILE_NAME
    fileMetadata.getMediaType() == MEDIA_TYPE
    fileMetadata.getContentDisposition() == Disposition.INLINE
    fileMetadata.getTotalLength() == CONTENT_LENGTH
  }

  def "Scenario: retrieve absent file metadata"() {
    expect: "The unknown file metadata shouldn't be retrieved"
    fileMetadataRepository.findById("UNKNOWN").isPresent() == false
  }

  def cleanup() {
    databaseConfigurer.tearDown()
  }
}
