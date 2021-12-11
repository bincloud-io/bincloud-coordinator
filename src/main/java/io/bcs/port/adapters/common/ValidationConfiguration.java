package io.bcs.port.adapters.common;

import io.bce.validation.DefaultValidationContext;
import io.bce.validation.GlobalValidations;
import io.bce.validation.ValidationService;
import io.bcs.DictionaryValidation.DictionaryPredicate;
import io.bcs.domain.model.validations.MediaTypeAcceptanceValidation;
import io.bcs.port.adapters.file.validation.JdbcMediaTypeAcceptancePredicate;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

@ApplicationScoped
public class ValidationConfiguration {
  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  @Produces
  public ValidationService validationService() {
    return DefaultValidationContext.createValidationService();
  }

  @Produces
  public DictionaryPredicate<String> mediaTypeAcceptancePredicate() {
    return new JdbcMediaTypeAcceptancePredicate(dataSource);
  }

  public void configureValidators(@Observes @Initialized(ApplicationScoped.class) Object init) {
    GlobalValidations.registerRule(MediaTypeAcceptanceValidation.RULE_ALIAS,
        MediaTypeAcceptanceValidation.createGlobalRule(mediaTypeAcceptancePredicate()));
  }
}
