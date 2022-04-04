package io.bcs.fileserver.domain.model;

/**
 * This interface describes the component, providing the current distribution point name.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface DistributionPointNameProvider {
  /**
   * Get current distribution point name.
   *
   * @return The distribution point name
   */
  String getDistributionPointName();
}