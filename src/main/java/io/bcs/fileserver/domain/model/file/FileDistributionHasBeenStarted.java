package io.bcs.fileserver.domain.model.file;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.file.File.CreatedFileState;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileContentLocator;
import java.time.LocalDateTime;
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
public class FileDistributionHasBeenStarted implements CreatedFileState {
  public static final EventType<FileDistributionHasBeenStarted> EVENT_TYPE = EventType
      .createFor("FILE_DISTRIBUTION_HAS_BEEN_STARTED", FileDistributionHasBeenStarted.class);

  private final ContentLocator contentLocator;
  private final String mediaType;
  private final String fileName;
  private final Long totalLength;
  private final LocalDateTime createdAt;

  /**
   * The event constructor.
   *
   * @param file The distributed file
   */
  public FileDistributionHasBeenStarted(File file) {
    super();
    this.contentLocator = new FileContentLocator(file);
    this.mediaType = file.getMediaType();
    this.fileName = file.getFileName();
    this.totalLength = file.getTotalLength();
    this.createdAt = file.getCreatedAt();
  }

  @Override
  public String getStorageFileName() {
    return contentLocator.getStorageFileName();
  }

  @Override
  public String getStorageName() {
    return contentLocator.getStorageName();
  }
}
