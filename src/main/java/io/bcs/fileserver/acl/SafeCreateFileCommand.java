package io.bcs.fileserver.acl;

import io.bce.validation.ValidationContext;
import io.bce.validation.ValidationContext.Validatable;
import io.bcs.fileserver.domain.model.file.File.CreateFile;
import io.bcs.fileserver.domain.validations.FileNameValidation;
import io.bcs.fileserver.domain.validations.MediaTypeAcceptanceValidation;
import io.bcs.fileserver.domain.validations.MediaTypeValidation;

/**
 * This class implements the safe file creation command. It implements Anti-corruptions layer
 * inside, based on the {@link ValidationContext}.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public abstract class SafeCreateFileCommand implements CreateFile, Validatable {
  private static final String MEDIA_TYPE_GROUP = "mediaType";
  private static final String FILE_NAME_GROUP = "fileName";

  @Override
  public ValidationContext validate(ValidationContext context) {
    return context.validate(MEDIA_TYPE_GROUP, new MediaTypeValidation(getMediaType().orElse(null)))
        .validate(MEDIA_TYPE_GROUP, new MediaTypeAcceptanceValidation(getMediaType().orElse(null)))
        .validate(FILE_NAME_GROUP, new FileNameValidation(getFileName().orElse(null)));
  }
}
