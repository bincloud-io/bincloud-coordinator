package io.bcs.port.adapters.file

import static io.bcs.domain.model.file.FileStatus.DISTRIBUTING
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

import io.bce.Locker
import io.bcs.domain.model.Constants
import io.bcs.domain.model.file.ContentLocator
import io.bcs.domain.model.file.File
import io.bcs.domain.model.file.FileMetadata
import io.bcs.domain.model.file.FileRepository
import io.bcs.domain.model.file.FileStatus
import io.bcs.testing.archive.ArchiveBuilder
import io.bcs.testing.database.DatabaseConfigurer
import io.bcs.testing.database.jdbc.cdi.JdbcLiquibase
import spock.lang.Specification

@RunWith(ArquillianSputnik)
class JpaFileRepositoryITSpec extends Specification {
    private static final String BCE_PACKAGE_NAME = Locker.getPackage().getName();
    private static final String BCE_DATABASE_CONFIGURER_PACKAGE = DatabaseConfigurer.getPackage().getName()
    private static final String BCS_DOMAIN_MODEL_PACKAGE = Constants.getPackage().getName()

    private static final String FILE_STORAGE_NAME = "file.storage.name.12345"
    private static final String FILE_STORAGE = "test-storage"
    private static final String MEDIA_TYPE = "application/json"
    private static final String FILE_NAME = "file.txt"
    private static final Long CONTENT_LENGTH = 1000L


    @Deployment
    public static Archive "create deployment"() {
        return ArchiveBuilder.jar("jpa-file-repository-spec.jar")
                .resolveDependencies("pom.xml")
                .withScopes(COMPILE, RUNTIME, TEST)
                .resolveDependency("org.liquibase", "liquibase-core")
                .apply()
                .appendPackagesRecursively(BCE_PACKAGE_NAME)
                .appendPackagesRecursively(BCE_DATABASE_CONFIGURER_PACKAGE)
                .appendPackagesRecursively(BCS_DOMAIN_MODEL_PACKAGE)
                .appendClasses(JpaFileRepository)
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
        
    private FileRepository fileRepository;

    def setup() {
        databaseConfigurer.setup("liquibase/master.changelog.xml")
        this.fileRepository = new JpaFileRepository(entityManager, transactionManager)
    }

    def cleanup() {
        databaseConfigurer.tearDown()
    }

    def "Scenario: store file entity"() {
        given: "The file entity to save"
        File file = createFileEntity()
        
        when: "The file entity is saved"
        fileRepository.save(file)
        
        and: "The stored entity is retreved by id"
        file = fileRepository.findById(FILE_STORAGE_NAME).get()
        
        then: "The found file state should be equal to the state before save"
        ContentLocator locator = file.getLocator();
        FileMetadata metadata = file.getFileMetadata()
        locator.getStorageName() == FILE_STORAGE
        locator.getStorageFileName() == FILE_STORAGE_NAME
        metadata.getStatus() == FileStatus.DISTRIBUTING
        metadata.getFileName() == FILE_NAME
        metadata.getMediaType() == MEDIA_TYPE
        metadata.getTotalLength() == CONTENT_LENGTH
    }

    private File createFileEntity() {
        return File.builder()
                .storageFileName(FILE_STORAGE_NAME)
                .storageName(FILE_STORAGE)
                .status(DISTRIBUTING)
                .mediaType(MEDIA_TYPE)
                .fileName(FILE_NAME)
                .contentLength(CONTENT_LENGTH)
                .build()
    }
}
