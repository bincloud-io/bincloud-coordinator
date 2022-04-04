package io.bcs.fileserver.infrastructure.file;

import io.bce.Generator;
import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for file storage name generating.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class StorageFileNameGenerator implements Generator<String> {
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  public String generateNext() {
    return String.format("%s--%s-%s", distributionPointNameProvider.getDistributionPointName(),
        Thread.currentThread().getId(), UUID.randomUUID());
  }
}
