package io.bcs.fileserver.domain.model.storage.descriptor;

import io.bcs.fileserver.domain.model.storage.StorageType;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class implements the base storage descriptor entity.
 *
 * @author Dmitry Mikhayklenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class StorageDescriptor {
  static final String DEFAULT_STORAGE_NAME = "unknown";
  static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";

  @Include
  @Default
  private String storageName = DEFAULT_STORAGE_NAME;

  @Default
  private String mediaType = DEFAULT_MEDIA_TYPE;
  
  public abstract StorageType getType();
}
