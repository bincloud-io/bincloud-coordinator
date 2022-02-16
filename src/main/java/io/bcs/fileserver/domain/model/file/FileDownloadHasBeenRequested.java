package io.bcs.fileserver.domain.model.file;

import io.bce.domain.EventType;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This event notifies that the file download has been requested.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@RequiredArgsConstructor
public class FileDownloadHasBeenRequested {
  public static final EventType<FileDownloadHasBeenRequested> EVENT_TYPE =
      EventType.createFor("FILE_DOWNLOAD_HAS_BEEN_REQUESTED", FileDownloadHasBeenRequested.class);
  private final Optional<String> storageFileName;
}
