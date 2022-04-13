package io.bcs.fileserver.infrastructure.config;

import io.bce.domain.EventBus;
import io.bce.domain.EventBus.EventSubscribtion;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.events.FileHasBeenCreated;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.storage.FileStorageProvider;
import io.bcs.fileserver.domain.model.storage.StorageType;
import io.bcs.fileserver.domain.model.storage.types.LocalStorageDescriptor;
import io.bcs.fileserver.domain.model.storage.types.RemoteStorageDescriptor;
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler;
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler.DistribuionPointsProvider;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.file.JdbcReplicationPointsProvider;
import io.bcs.fileserver.infrastructure.storage.FilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.JdbcFilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.local.FilesystemPhysicalFile;
import io.bcs.fileserver.infrastructure.storage.local.LocalFilesystemStorageFactory;
import io.bcs.fileserver.infrastructure.storage.remote.RemoteFilesystemStorageFactory;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

/**
 * This class configures file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class FileStorageConfiguration {
  @PersistenceContext(unitName = "central")
  private EntityManager entityManager;

  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  @Inject
  private FileRepository fileRepository;

  @Inject
  private FileServerConfigurationProperties fileServerConfigurationProperties;

  @Inject
  private EventBus eventBus;

  private final List<EventSubscribtion> subscribtions = new ArrayList<>();

  /**
   * The file system space manager configuration.
   *
   * @return The file system space manager
   */
  @Produces
  public FilesystemSpaceManager fileSpaceManager() {
    return new JdbcFilesystemSpaceManager(dataSource, fileServerConfigurationProperties);
  }

  /**
   * The local file storage provider configuration.
   *
   * @return The local file storage provider
   */
  @Produces
  public FileStorageProvider<LocalStorageDescriptor> localFileStorageProvider() {
    return new LocalFilesystemStorageFactory(fileSpaceManager(), FilesystemPhysicalFile.factory(),
        fileServerConfigurationProperties);
  }

  /**
   * The remote file storage provider configuration.
   *
   * @return The remote file storage provider
   */
  @Produces
  public FileStorageProvider<RemoteStorageDescriptor> remoteFileStorageProvider() {
    return new RemoteFilesystemStorageFactory(fileServerConfigurationProperties);
  }

  /**
   * Replication points provider configuration.
   *
   * @return The replication points provider.
   */
  @Produces
  public DistribuionPointsProvider replicationPointsProvider() {
    return new JdbcReplicationPointsProvider(dataSource);
  }

  /**
   * Created file synchronization handler configuration.
   *
   * @return The file synchronization handler
   */
  @Produces
  public CreatedFileSynchronizationHandler createdFileSynchronizationHandler() {
    return new CreatedFileSynchronizationHandler(fileRepository, replicationPointsProvider());
  }

  /**
   * Register storage providers.
   *
   * @param init The CDI context event
   */
  public void configureStorages(@Observes @Initialized(ApplicationScoped.class) Object init) {
    StorageType.LOCAL.registerFileStorageProvider(localFileStorageProvider());
    StorageType.REMOTE.registerFileStorageProvider(remoteFileStorageProvider());
  }

  /**
   * Register domain events.
   *
   * @param init The CDI context event
   */
  public void configureEvents(@Observes @Initialized(ApplicationScoped.class) Object init) {
    this.subscribtions.add(eventBus.subscribeOn(Constants.CONTEXT, FileHasBeenCreated.EVENT_TYPE,
        createdFileSynchronizationHandler()));
  }

  /**
   * Destroy domain events.
   *
   * @param destroy The CDI context event
   */
  public void unsubscribeEvents(@Observes @Destroyed(ApplicationScoped.class) Object destroy) {
    subscribtions.stream().forEach(EventSubscribtion::unsubscribe);
  }
}
