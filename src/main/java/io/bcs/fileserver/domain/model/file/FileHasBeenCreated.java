package io.bcs.fileserver.domain.model.file;

import io.bce.domain.EventType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This event notifies that the file has been disposed.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@RequiredArgsConstructor
public class FileHasBeenCreated {
  public static final EventType<FileHasBeenCreated> EVENT_TYPE =
      EventType.createFor("FILE_HAS_BEEN_CREATED", FileHasBeenCreated.class);
  private final String storageFileName;
  private final String mediaType;
}
