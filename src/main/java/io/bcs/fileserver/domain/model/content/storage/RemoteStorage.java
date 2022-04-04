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
public class RemoteStorage extends StorageDescriptor {
  private String remoteStorageGatewayWsdl;
  
  @Override
  public StorageType getType() {
    return StorageType.REMOTE;
  }
}
