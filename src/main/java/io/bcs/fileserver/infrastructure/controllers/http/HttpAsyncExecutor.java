package io.bcs.fileserver.infrastructure.controllers.http;

import java.util.function.Consumer;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for servlet request asynchronous handling execution.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(staticName = "of")
public class HttpAsyncExecutor {
  private final HttpServletRequest servletRequest;
  private final HttpServletResponse servletResponse;

  /**
   * Execute web method asynchronously.
   *
   * @param methodExecutor The web method executor.
   */
  public void executeAsynchronously(Consumer<AsyncContext> methodExecutor) {
    methodExecutor.accept(servletRequest.startAsync(servletRequest, servletResponse));
  }
}
