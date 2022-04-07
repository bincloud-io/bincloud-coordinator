package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This event notfies that the file distribution has been started.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@SuperBuilder
public class FileDistributionHasBeenStarted implements FileUploadStatistic {
  public static final EventType<FileDistributionHasBeenStarted> EVENT_TYPE = EventType
      .createFor("FILE_DISTRIBUTION_HAS_BEEN_STARTED", FileDistributionHasBeenStarted.class);

  @Getter
  private final String distributionPointName;
  private final String storageFileName;
  private final String storageName;
  @Getter
  private final Long totalLength;

  /**
   * The event constructor.
   *
   * @param file The distributing file
   */
  public FileDistributionHasBeenStarted(File file) {
    super();
    this.distributionPointName = file.getDistributionPoint();
    this.storageFileName = file.getStorageFileName();
    this.storageName = file.getStorageName().get();
    this.totalLength = file.getTotalLength();
  }

  @Override
  public ContentLocator getLocator() {
    return new DefaultContentLocator(storageFileName, storageName);
  }
}
