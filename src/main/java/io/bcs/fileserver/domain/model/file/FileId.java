package io.bcs.fileserver.domain.model.file;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * This class describes the file identifier value object.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FileId implements Serializable {
  private static final long serialVersionUID = -2650510282705807841L;

  @NonNull
  private String distributionPoint;

  @NonNull
  private String storageFileName;
}
