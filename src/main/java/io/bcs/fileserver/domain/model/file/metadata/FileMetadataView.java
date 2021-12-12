package io.bcs.fileserver.domain.model.file.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This interface describes the file metadata view model.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FileMetadataView implements FileMetadata {
  @EqualsAndHashCode.Include
  private String storageFileName;
  private String fileName;
  private Long totalLength;
  private String mediaType;
  private Disposition contentDisposition;
}
