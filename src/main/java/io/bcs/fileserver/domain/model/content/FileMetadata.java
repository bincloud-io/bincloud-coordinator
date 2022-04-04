package io.bcs.fileserver.domain.model.content;

import io.bcs.fileserver.domain.model.file.Disposition;

/**
 * This class describes the file metadata.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileMetadata {
  String getMediaType();

  String getFileName();

  Long getTotalLength();

  Disposition getContentDisposition();
}