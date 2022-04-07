package io.bcs.fileserver.domain.model.storage.types;

import io.bcs.fileserver.domain.model.storage.StorageDescriptor;
import io.bcs.fileserver.domain.model.storage.StorageType;
import java.net.URL;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class describes the remote storage descriptor type.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class RemoteStorageDescriptor extends StorageDescriptor {
  private String remoteStorageGatewayWsdl;

  @Override
  public StorageType getType() {
    return StorageType.REMOTE;
  }
  
  @SneakyThrows
  public URL getRemoteStorageGatewayWsdl() {
    return new URL(remoteStorageGatewayWsdl);
  }
}
