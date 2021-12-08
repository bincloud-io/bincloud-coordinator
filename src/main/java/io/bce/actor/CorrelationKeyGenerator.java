package io.bce.actor;

import java.util.UUID;

import io.bce.Generator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CorrelationKeyGenerator implements Generator<CorrelationKey> {
  @NonNull
  private final String instanceId;

  @Override
  public CorrelationKey generateNext() {
    return CorrelationKey.wrap(String.format("%s:%s", instanceId, UUID.randomUUID()));
  }
}
