package io.bcs.domain.model.file.states.lifecycle;

import io.bce.domain.errors.ApplicationException;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InacceptableLifecycleMethod<R> implements LifecycleMethod<R> {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(InacceptableLifecycleMethod.class);

  private final ExceptionProvider exceptionProvider;

  @Override
  public Promise<R> execute() {
    ApplicationException error = exceptionProvider.createException();
    log.warn(TextTemplates.createBy("The operation is inacceptable. Operation is going "
        + "to be completed with {{error}} error").withParameter("error", error));
    return Promises.rejectedBy(error);
  }

  public interface ExceptionProvider {
    public ApplicationException createException();
  }
}
