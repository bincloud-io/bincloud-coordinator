package io.bcs.fileserver.domain.events;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.file.File;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This event notfies that the file has been disposed.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@RequiredArgsConstructor
public class FileHasBeenDisposed {
  public static final EventType<FileHasBeenDisposed> EVENT_TYPE =
      EventType.createFor("FILE_HAS_BEEN_DISPOSED", FileHasBeenDisposed.class);
  private final String storageFileName;

  /**
   * Create the event.
   *
   * @param file The disposed file
   */
  public FileHasBeenDisposed(File file) {
    super();
    this.storageFileName = file.getStorageFileName();
  }
}
