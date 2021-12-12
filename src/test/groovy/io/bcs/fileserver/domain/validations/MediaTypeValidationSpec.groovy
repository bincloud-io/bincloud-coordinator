package io.bcs.fileserver.domain.validations

import io.bce.validation.UngroupedValidationCase
import io.bce.validation.ValidationCase
import io.bce.validation.ValidationSpecification
import io.bce.validation.ValidationCase.ExpectedResult
import io.bcs.fileserver.domain.validations.MediaTypeValidation

class MediaTypeValidationSpec extends ValidationSpecification {
    static final String WRONG_FORMAT_ERROR_MESSAGE = "media.type.wrong.format.error";
    static final String WRONG_SIZE_ERROR_MESSAGE = "media.type.wrong.length.error";
    
    private static final String LONG_MEDIA_TYPE = "application/vdJR0YsN0B529zD1rbPS" +
    "gwKNyrpLBpIUYArpdXJMIL5YHwDEP9TgjiiZArjO3dA1LKGrhVp4Rx7YscRI7BuhcfatMpYtZfVL4K16R" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" + 
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "QkptWgaV5FC1WFSORdOLkEg3osICQF7Wgxk5p"
    
    private static final String BAD_FORMATTED_TYPE = "Bad Formatted Type"
    
    private static final String WELL_FORMATTED_TYPE = "application/xml"
    
    @Override
    protected Collection<ValidationCase> getValidationCases() {
        return [
            new UngroupedValidationCase(
                new MediaTypeValidation(null), 
                ExpectedResult.PASSED, 
                []
            ),
            new UngroupedValidationCase(
                new MediaTypeValidation(WELL_FORMATTED_TYPE),
                ExpectedResult.PASSED,
                []
            ),
            new UngroupedValidationCase(
                new MediaTypeValidation(LONG_MEDIA_TYPE),
                ExpectedResult.FAILED,
                [WRONG_SIZE_ERROR_MESSAGE]
            ),
            new UngroupedValidationCase(
                new MediaTypeValidation(""),
                ExpectedResult.FAILED,
                [WRONG_SIZE_ERROR_MESSAGE]
            ),
            new UngroupedValidationCase(
                new MediaTypeValidation(BAD_FORMATTED_TYPE), 
                ExpectedResult.FAILED,
                [WRONG_FORMAT_ERROR_MESSAGE]
            ),
        ];
    }
}
