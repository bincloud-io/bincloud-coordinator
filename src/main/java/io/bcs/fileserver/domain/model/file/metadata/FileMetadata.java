package io.bcs.fileserver.domain.model.file.metadata;

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