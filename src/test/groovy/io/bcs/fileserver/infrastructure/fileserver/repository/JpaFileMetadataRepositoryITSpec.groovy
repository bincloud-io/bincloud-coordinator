package io.bcs.fileserver.infrastructure.fileserver.repository

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.spock.ArquillianSputnik
import org.jboss.shrinkwrap.api.Archive
import org.junit.runner.RunWith
import io.bce.CriticalSection
import io.bcs.fileserver.Constants
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata
import io.bcs.fileserver.domain.model.file.metadata.FileMetadataRepository
import io.bcs.fileserver.infrastructure.repositories.JpaFileMetadataRepository
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JpaFileMetadataRepositoryITSpec extends Specification {
  private static final String BCE_PACKAGE_NAME = CriticalSection.getPackage().getName();
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
        .apply()
        .appendPackagesRecursively(BCE_PACKAGE_NAME)
        .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(JpaFileMetadataRepository)
        .appendManifestResource("META-INF/beans.xml", "beans.xml")
        .appendManifestResource("jpa-test/file-mapping-persistence.xml", "persistence.xml")
        .appendManifestResource("jpa-test/file-mapping-persistence.xml", "persistence.xml")
        .appendManifestResource("META-INF/orm/file-mapping.xml", "orm/file-mapping.xml")
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
    databaseConfigurer.setup("db-init/file/file.metadata.changelog.xml")

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
