package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.content.ContentLocator;
import io.bcs.fileserver.domain.model.file.File;
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
  private final Long totalLength;

  /**
   * The event constructor.
   *
   * @param file The distributing file
   */
  public FileDistributionHasBeenStarted(File file) {
    super();
    this.storageFileName = file.getStorageFileName();
    this.storageName = file.getStorageName().get();
    this.totalLength = file.getTotalLength();
  }
}
