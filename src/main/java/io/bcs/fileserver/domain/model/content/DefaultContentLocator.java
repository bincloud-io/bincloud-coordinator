package io.bcs.fileserver.domain.model.content;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class is the default content locator implementation.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DefaultContentLocator implements ContentLocator {
  private final String storageFileName;
  private final String storageName;
}
