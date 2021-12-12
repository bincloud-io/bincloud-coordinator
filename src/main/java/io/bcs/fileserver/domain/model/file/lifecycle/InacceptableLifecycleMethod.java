package io.bcs.fileserver.domain.model.file.lifecycle;

import io.bce.domain.errors.ApplicationException;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

/**
 * This method implements the mechanism of breaking any attempts over the file entity. Any attempt
 * to execute this method will be rejected.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <R> The response type name
 */
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

  /**
   * This interface describes an exception providing mechanism.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface ExceptionProvider {
    public ApplicationException createException();
  }
}
