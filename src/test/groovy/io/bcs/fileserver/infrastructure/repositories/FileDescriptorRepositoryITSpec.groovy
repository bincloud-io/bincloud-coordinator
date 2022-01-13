package io.bcs.fileserver.infrastructure.repositories

import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.COMPILE
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.RUNTIME
import static org.jboss.shrinkwrap.resolver.api.maven.ScopeType.TEST

import io.bcs.fileserver.domain.Constants
import io.bcs.fileserver.domain.model.file.File
import io.bcs.fileserver.domain.model.file.FileDescriptor
import io.bcs.fileserver.domain.model.file.FileDescriptorRepository
import io.bcs.fileserver.domain.model.file.FileRepository
import io.bcs.fileserver.domain.model.file.state.FileStatus
import io.bcs.fileserver.domain.model.storage.ContentLocator
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
class FileDescriptorRepositoryITSpec extends Specification {
  private static final String REF_MEDIATYPES_MIGRATION_SCRIPT ="db-init/file/ref.mediatypes.config.changelog.xml"
  private static final String REF_LOCAL_STORAGES_MIGRATION_SCRIPT = "db-init/file/ref.local.storages.config.changelog.xml"
  private static final String BCE_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
  private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()

  private static final String FILE_STORAGE_NAME = "file.storage.name.12345"
  private static final String FILE_STORAGE = "LOCAL"
  private static final String MEDIA_TYPE = "application/mediatype"
  private static final String FILE_NAME = "file.txt"
  private static final Long CONTENT_LENGTH = 1000L
  
  @Deployment
  public static Archive "create deployment"() {
    return ArchiveBuilder.jar("jpa-file-descriptor-repository-spec.jar")
        .resolveDependencies("pom.xml")
        .withScopes(COMPILE, RUNTIME, TEST)
        .resolveDependency("org.liquibase", "liquibase-core")
        .resolveDependency("io.bce", "bce")
        .resolveDependency("io.bce", "bce-test-kit")
        .resolveDependency("io.bce", "bce-spock-ext")
        .apply()
        .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
        .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
        .appendClasses(JpaFileDescriptorRepository)
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

  private FileDescriptorRepository fileDescriptorRepository;

  def setup() {
    databaseConfigurer.setup("liquibase/master.changelog.xml")
    this.fileDescriptorRepository = new JpaFileRepository(entityManager, transactionManager)
  }

  def cleanup() {
    databaseConfigurer.tearDown()
  }

  def "Scenario: store file entity"() {
    given: "The file media types and local storage is configured"
    databaseConfigurer.setup(REF_MEDIATYPES_MIGRATION_SCRIPT)
    databaseConfigurer.setup(REF_LOCAL_STORAGES_MIGRATION_SCRIPT)

    and: "The file entity to save"
    FileDescriptor fileDescriptor = createFileDescriptorEntity()

    when: "The file entity is saved"
    fileDescriptorRepository.save(fileDescriptor)

    and: "The stored entity is retreved by id"
    fileDescriptor = fileDescriptorRepository.findById(FILE_STORAGE_NAME).get()

    then: "The found file state should be equal to the state before save"
    fileDescriptor.getStorageName() == FILE_STORAGE
    fileDescriptor.getStorageFileName() == FILE_STORAGE_NAME
    fileDescriptor.getStatus() == FileStatus.DISTRIBUTING
    fileDescriptor.getFileName() == FILE_NAME
    fileDescriptor.getMediaType() == MEDIA_TYPE
    fileDescriptor.getTotalLength() == CONTENT_LENGTH
  }

  private FileDescriptor createFileDescriptorEntity() {
    return FileDescriptor.builder()
        .storageFileName(FILE_STORAGE_NAME)
        .storageName(FILE_STORAGE)
        .status(FileStatus.DRAFT)
        .mediaType(MEDIA_TYPE)
        .fileName(FILE_NAME)
        .totalLength(CONTENT_LENGTH)
        .build()
  }
}
