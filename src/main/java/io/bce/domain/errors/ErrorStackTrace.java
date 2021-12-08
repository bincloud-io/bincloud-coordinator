package io.bce.domain.errors;

import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for the working with the stack trace.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class ErrorStackTrace {
  @NonNull
  private final Throwable stacktraceOwner;

  @Override
  public String toString() {
    StringWriter stringWriter = new StringWriter();
    stacktraceOwner.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}
