package io.bcs.domain.model.file;

public interface FileMetadata {
    public FileStatus getStatus();

    public String getMediaType();

    public String getFileName();

    public Long getTotalLength();

    public Disposition getDefaultDisposition();

    public enum Disposition {
        INLINE, ATTACHMENT;
    }
}