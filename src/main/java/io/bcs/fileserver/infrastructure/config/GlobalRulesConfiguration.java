package io.bcs.fileserver.infrastructure.config;

import io.bce.validation.GlobalValidations;
import io.bcs.fileserver.domain.validations.DictionaryValidation.DictionaryPredicate;
import io.bcs.fileserver.domain.validations.MediaTypeAcceptanceValidation;
import io.bcs.fileserver.infrastructure.validations.JdbcMediaTypeAcceptancePredicate;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;

/**
 * This class contains file server global validation rules configuration.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class GlobalRulesConfiguration {
  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  @Produces
  public DictionaryPredicate<String> mediaTypeAcceptancePredicate() {
    return new JdbcMediaTypeAcceptancePredicate(dataSource);
  }

  /**
   * Register global validations.
   *
   * @param init The CDI context event
   */
  public void configureValidators(@Observes @Initialized(ApplicationScoped.class) Object init) {
    GlobalValidations.registerRule(MediaTypeAcceptanceValidation.RULE_ALIAS,
        MediaTypeAcceptanceValidation.createGlobalRule(mediaTypeAcceptancePredicate()));
  }
}
