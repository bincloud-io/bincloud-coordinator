package io.bcs.application.acl;

import io.bce.validation.ValidationContext;
import io.bce.validation.ValidationContext.Validatable;
import io.bcs.domain.model.file.File.CreateFile;
import io.bcs.domain.model.validations.FileNameValidation;
import io.bcs.domain.model.validations.MediaTypeValidation;

public abstract class SafeCreateFileCommand implements CreateFile, Validatable {

    private static final String MEDIA_TYPE_GROUP = "mediaType";
    private static final String FILE_NAME_GROUP = "fileName";

    @Override
    public ValidationContext validate(ValidationContext context) {
        return context.validate(MEDIA_TYPE_GROUP, new MediaTypeValidation(getMediaType().orElse(null)))
                .validate(MEDIA_TYPE_GROUP, new MediaTypeValidation(getMediaType().orElse(null)))
                .validate(FILE_NAME_GROUP, new FileNameValidation(getFileName().orElse(null)));
    }
}
