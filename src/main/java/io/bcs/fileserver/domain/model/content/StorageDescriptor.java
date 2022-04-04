package io.bcs.fileserver.domain.model.content;

import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class StorageDescriptor {
  private static final String DEFAULT_STORAGE_NAME = "UNKNOWN";
  private static final String DEFAULT_DISTRIBUTION_POINT = "DEFAULT";

  @Default
  @Include
  private String storageName = DEFAULT_STORAGE_NAME;

  @Default
  @Include
  private String distributionPoint = DEFAULT_DISTRIBUTION_POINT;

  public static StorageDescriptor unknownStorage() {
    return new UnknownStorage();
  }

  public FileStorage getStorage() {
    return getType().getRegisteredFileStorageProvider().getFileStorage(this);
  }

  protected abstract StorageType getType();

  @NoArgsConstructor
  private static class UnknownStorage extends StorageDescriptor {
    @Override
    protected StorageType getType() {
      return StorageType.UNKNOWN;
    }
  }
}
