package io.bcs.fileserver.domain.model.file;

import io.bce.domain.EventType;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
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
  private final ContentLocator contentLocator;
}
