package io.bcs.common.config;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.JulLogger;
import io.bce.logging.Loggers;
import io.bce.text.TextProcessor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * This class configures logging system.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class LoggingConfiguration {
  @Inject
  private TextProcessor textProcessor;

  /**
   * The application logger configuration.
   *
   * @return The application logger
   */
  @Produces
  public ApplicationLogger applicationLogger() {
    return new JulLogger(textProcessor);
  }

  /**
   * Register logger globally.
   *
   * @param init The CDI context event
   */
  public void initializeLogger(@Observes @Initialized(ApplicationScoped.class) Object init) {
    Loggers.registry().registerApplicationLogger(applicationLogger());
  }
}
