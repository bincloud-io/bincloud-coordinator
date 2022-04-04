package io.bcs.fileserver.infrastructure;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;

/**
 * This interface describes provided configuration properties.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileServerConfigurationProperties extends DistributionPointNameProvider {
  int getBufferSize();
  
  String getPublicBaseUrlAddress();
  
  String getPrivateBaseUrlAddress();

  Long getSyncOperationTimeout();
}
