package io.bcs.fileserver.domain.model.content;

import java.util.Optional;

/**
 * This interface describes a content upload command.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface UploadCommand {
  /**
   * Get storage file name.
   *
   * @return The storage file name
   */
  Optional<String> getStorageFileName();

  /**
   * The file content length.
   *
   * @return The content length
   */
  Long getContentLength();
}
