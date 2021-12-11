package io.bcs.port.adapters.common;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.JulLogger;
import io.bce.logging.Loggers;
import io.bce.text.TextProcessor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class LoggingConfiguration {
  @Inject
  private TextProcessor textProcessor;

  @Produces
  public ApplicationLogger applicationLogger() {
    return new JulLogger(textProcessor);
  }

  public void initializeLogger(@Observes @Initialized(ApplicationScoped.class) Object init) {
    Loggers.registry().registerApplicationLogger(applicationLogger());
  }
}
