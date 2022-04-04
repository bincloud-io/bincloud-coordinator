package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.content.ContentLocator;
import io.bcs.fileserver.domain.model.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * This event notifies that the file content has been uploaded, but file distributing hasn't been
 * started.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@SuperBuilder
@AllArgsConstructor
public class FileContentHasBeenUploaded implements FileUploadStatistic {
  public static final EventType<FileContentHasBeenUploaded> EVENT_TYPE =
      EventType.createFor("FILE_CONTENT_HAS_BEEN_UPLOADED", FileContentHasBeenUploaded.class);

  private String storageName;
  private String storageFileName;
  @Getter
  private Long totalLength;

  /**
   * Create the domain event.
   *
   * @param statistic A file upload statistic
   */
  public FileContentHasBeenUploaded(FileUploadStatistic statistic) {
    super();
    this.storageName = statistic.getLocator().getStorageName();
    this.storageFileName = statistic.getLocator().getStorageFileName();
    this.totalLength = statistic.getTotalLength();
  }

  @Override
  public ContentLocator getLocator() {
    return new DefaultContentLocator(storageFileName, storageName);
  }
}
