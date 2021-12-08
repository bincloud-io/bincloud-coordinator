package io.bce.interaction.polling;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.experimental.UtilityClass;

/**
 * This class is responsible for creating different standard data pollers configurations. s
 *
 * @author Dmitry Mikhaylenko
 *
 */
@UtilityClass
public class Polling {
  /**
   * Create sequential data polling stream without ordering information.
   *
   * @param <D>             The polling data type name
   * @param pollingFunction The polling function
   * @return The stream of polled data
   */
  public <D> Stream<D> sequentialPolling(BatchPoller<D> pollingFunction) {
    return sequentialNumeratedPolling(pollingFunction).map(PolledElement::getData);
  }

  /**
   * Create parallel data polling stream without ordering information.
   *
   * @param <D>             The polling data type name
   * @param pollingFunction The polling function
   * @return The stream of polled data
   */
  public <D> Stream<D> parallelPolling(BatchPoller<D> pollingFunction) {
    return parallelNumeratedPolling(pollingFunction).map(PolledElement::getData);
  }

  /**
   * Create sequential data polling stream with ordering information.
   *
   * @param <D>             The polling data type name
   * @param pollingFunction The polling function
   * @return The stream of polled elements {@link PolledElement}
   */
  public <D> Stream<PolledElement<D>> sequentialNumeratedPolling(BatchPoller<D> pollingFunction) {
    return polling(pollingFunction, false);
  }

  /**
   * Create parallell data polling without ordering information.
   *
   * @param <D>             The polling data type name
   * @param pollingFunction The polling function
   * @return The stream of polled elements {@link PolledElement}
   */
  public <D> Stream<PolledElement<D>> parallelNumeratedPolling(BatchPoller<D> pollingFunction) {
    return polling(pollingFunction, true);
  }

  private <D> Stream<PolledElement<D>> polling(BatchPoller<D> pollingFunction, boolean parallel) {
    DataPoller<D> dataPoller = new DataPoller<D>(pollingFunction);
    return StreamSupport.stream(dataPoller.spliterator(), parallel);
  }
}
