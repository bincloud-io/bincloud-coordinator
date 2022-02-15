package io.bcs.fileserver.infrastructure.file;

import io.bce.Generator;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for file storage name generating.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class StorageFileNameGenerator implements Generator<String> {
  private final Supplier<String> distributionPointNameProvider;

  @Override
  public String generateNext() {
    return String.format("%s--%s-%s", distributionPointNameProvider.get(),
        Thread.currentThread().getId(), UUID.randomUUID());
  }
}
