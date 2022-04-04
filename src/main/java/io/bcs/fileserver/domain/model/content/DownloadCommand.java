package io.bcs.fileserver.domain.model.content;

import io.bcs.fileserver.domain.model.file.Range;
import java.util.Collection;
import java.util.Optional;

/**
 * This interface describes a content download command.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface DownloadCommand {
  /**
   * Get storage file name.
   *
   * @return The storage file name
   */
  Optional<String> getStorageFileName();

  /**
   * Get content ranges.
   *
   * @return The content ranges
   */
  Collection<Range> getRanges();
}