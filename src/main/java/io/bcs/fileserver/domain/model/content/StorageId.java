package io.bcs.fileserver.domain.model.content;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The storage identifier value object.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StorageId implements Serializable {
  private static final long serialVersionUID = -7099813644648128948L;
  private static final String DEFAULT_STORAGE_NAME = "UNKNOWN";
  private static final String DEFAULT_DISTRIBUTION_POINT = "DEFAULT";

  @Include
  private String storageName = DEFAULT_STORAGE_NAME;

  @Include
  private String distributionPoint = DEFAULT_DISTRIBUTION_POINT;
}
