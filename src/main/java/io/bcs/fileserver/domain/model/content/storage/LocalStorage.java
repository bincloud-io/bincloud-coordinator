package io.bcs.fileserver.domain.model.content.storage;

import io.bcs.fileserver.domain.model.content.StorageDescriptor;
import io.bcs.fileserver.domain.model.content.StorageType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class LocalStorage extends StorageDescriptor {
  private String baseDirectory;
  private Long diskQuote;

  @Override
  public StorageType getType() {
    return StorageType.LOCAL;
  }
}
