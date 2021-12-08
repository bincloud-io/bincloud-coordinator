package io.bce;

/**
 * This error represents the error, notifying that something exception happened which mustn't be
 * happened.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class MustNeverBeHappenedError extends Error {
  private static final long serialVersionUID = -2357775532021757611L;

  /**
   * Create error initialized by error message, describing what kind of went wrong.
   *
   * @param message The error message
   */
  public MustNeverBeHappenedError(String message) {
    super(message);
  }

  /**
   * Create error initialized by throwable object describing the happened error.
   *
   * @param cause The throwable object
   */
  public MustNeverBeHappenedError(Throwable cause) {
    super(String.format("Error %s must never be happened for this case.", cause.getClass()), cause);
  }
}
