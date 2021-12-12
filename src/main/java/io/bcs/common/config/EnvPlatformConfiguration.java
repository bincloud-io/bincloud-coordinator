package io.bcs.common.config;

import io.bcs.common.PlatformConfigurationProperties;
import javax.enterprise.context.ApplicationScoped;

/**
 * This class provides platform configuration properties.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class EnvPlatformConfiguration implements PlatformConfigurationProperties {
  private static final String INSTANCE_ID_VAR = "BC_INSTANCE";

  @Override
  public String getInstanceId() {
    return System.getenv(INSTANCE_ID_VAR);
  }
}
