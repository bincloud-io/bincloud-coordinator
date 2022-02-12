package io.bcs.fileserver.infrastructure.config;

import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class provides file server configuration properties.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class EnvFileServerConfiguration implements FileServerConfigurationProperties {
  private static final String BUFFER_SIZE_VAR = "BC_IO_BUFFER_SIZE";
  private static final String BASE_PUBLIC_URL_VAR = "BC_BASE_PUBLIC_URL";
  private static final String BASE_PRIVATE_URL_VAR = "BC_BASE_PRIVATE_URL";
  private static final String DISTRIBUTION_POINT_NAME_VAR = "BC_DISTRIBUTION_POINT";
  private static final String SYNC_OPERATION_TIMEOUT_VAR = "BC_SYNC_OPERATION_TIMEOUT";

  @Override
  public String getPublicBaseUrlAddress() {
    return System.getenv(BASE_PUBLIC_URL_VAR);
  }

  @Override
  public String getPrivateBaseUrlAddress() {
    return System.getenv(BASE_PRIVATE_URL_VAR);
  }

  @Override
  public int getBufferSize() {
    return Integer.valueOf(System.getenv(BUFFER_SIZE_VAR));
  }

  @Override
  public String getDistributionPointName() {
    return System.getenv(DISTRIBUTION_POINT_NAME_VAR);
  }

  @Override
  public Long getSyncOperationTimeout() {
    return Long.valueOf(System.getenv(SYNC_OPERATION_TIMEOUT_VAR));
  }
}
