package io.bcs.domain.model.file.metadata;

import io.bcs.domain.model.file.FileMetadata;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

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
