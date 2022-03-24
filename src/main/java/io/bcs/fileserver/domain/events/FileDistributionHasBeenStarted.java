package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This event notfies that the file distribution has been started.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@SuperBuilder
public class FileDistributionHasBeenStarted implements ContentLocator {
  public static final EventType<FileDistributionHasBeenStarted> EVENT_TYPE = EventType
      .createFor("FILE_DISTRIBUTION_HAS_BEEN_STARTED", FileDistributionHasBeenStarted.class);

  private final String storageFileName;
  private final String storageName;

  /**
   * The event constructor.
   *
   * @param contentLocator The distributed file content locator
   */
  public FileDistributionHasBeenStarted(ContentLocator contentLocator) {
    super();
    this.storageFileName = contentLocator.getStorageFileName();
    this.storageName = contentLocator.getStorageName();
  }
}
