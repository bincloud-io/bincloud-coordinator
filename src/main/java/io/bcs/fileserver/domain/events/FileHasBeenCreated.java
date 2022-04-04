package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.File.CreatedFileState;
import java.time.LocalDateTime;
import lombok.Getter;

/**
 * This event notifies that the file has been disposed.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
public class FileHasBeenCreated implements CreatedFileState {
  public static final EventType<FileHasBeenCreated> EVENT_TYPE =
      EventType.createFor("FILE_HAS_BEEN_CREATED", FileHasBeenCreated.class);
  private final String distributionPoint;
  private final String storageFileName;
  private final String mediaType;
  private final String fileName;
  private final LocalDateTime createdAt;

  /**
   * Create event for specified file.
   *
   * @param file The created file
   */
  public FileHasBeenCreated(File file) {
    super();
    this.distributionPoint = file.getDistributionPoint();
    this.storageFileName = file.getStorageFileName();
    this.mediaType = file.getMediaType();
    this.fileName = file.getFileName();
    this.createdAt = file.getCreatedAt();
  }
}
