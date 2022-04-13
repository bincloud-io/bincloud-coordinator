package io.bcs.fileserver.domain.model.file.content.download;

import io.bcs.fileserver.domain.model.file.Disposition;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * This class implements the file media type entity.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MediaType {
  static final String DEFAULT_MEDIA_TYPE = "application/octet-stream";
  @Include
  @Default
  private String mediaType = DEFAULT_MEDIA_TYPE;

  @Default
  private Disposition disposition = Disposition.ATTACHMENT;
}
