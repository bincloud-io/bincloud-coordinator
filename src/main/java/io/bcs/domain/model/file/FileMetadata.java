package io.bcs.domain.model.file;

public interface FileMetadata {
  public String getMediaType();

  public String getFileName();

  public Long getTotalLength();

  public Disposition getContentDisposition();

  public enum Disposition {
    INLINE,
    ATTACHMENT;
  }
}