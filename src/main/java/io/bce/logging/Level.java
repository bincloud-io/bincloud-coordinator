package io.bce.logging;

import lombok.RequiredArgsConstructor;

/**
 * This class enumerates all available log levels.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public enum Level {
  CRITIC,
  ERROR,
  WARN,
  INFO,
  DEBUG,
  TRACE;
}