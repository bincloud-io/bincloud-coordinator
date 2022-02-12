package io.bcs.fileserver.infrastructure;

/**
 * This interface describes provided configuration properties.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileServerConfigurationProperties {
  int getBufferSize();
  
  String getDistributionPointName();

  String getPublicBaseUrlAddress();
  
  String getPrivateBaseUrlAddress();

  Long getSyncOperationTimeout();
}
