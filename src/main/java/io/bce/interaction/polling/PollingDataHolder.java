package io.bce.interaction.polling;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class PollingDataHolder<D> implements PolledElement<D> {
  private final Long index;
  private final D data;
}