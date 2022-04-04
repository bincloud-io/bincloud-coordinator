package io.bcs.fileserver.domain.model.storage.descriptor;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * This class describes the storage descriptor identifier value object.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StorageDescriptorId implements Serializable {
  private static final long serialVersionUID = -7972518089002808281L;

  @NonNull
  private String distributionPoint;
  @NonNull
  private String storageName;
}
