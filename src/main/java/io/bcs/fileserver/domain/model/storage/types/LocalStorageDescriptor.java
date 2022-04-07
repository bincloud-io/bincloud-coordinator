package io.bcs.fileserver.domain.model.storage.types;

import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import io.bcs.fileserver.domain.model.storage.StorageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class describes the local storage descriptor type.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class LocalStorageDescriptor extends StorageDescriptor {
  private String baseDirectory;
  private Long diskQuote;

  @Override
  public StorageType getType() {
    return StorageType.LOCAL;
  }
}
