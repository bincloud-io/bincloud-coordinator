package io.bcs.common.config;

import io.bce.validation.DefaultValidationContext;
import io.bce.validation.ValidationService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * This class configures the validation server.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class ValidationConfiguration {
  /**
   * The validation service configuration.
   *
   * @return The validation service
   */
  @Produces
  public ValidationService validationService() {
    return DefaultValidationContext.createValidationService();
  }
}
