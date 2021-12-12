package io.bcs.fileserver.domain.validations

import io.bce.validation.UngroupedValidationCase
import io.bce.validation.ValidationCase
import io.bce.validation.ValidationSpecification
import io.bce.validation.ValidationCase.ExpectedResult
import io.bcs.fileserver.domain.validations.FileNameValidation

class FileNameValidationSpec extends ValidationSpecification {
    static final String WRONG_FORMAT_ERROR_MESSAGE = "file.name.wrong.format.error";
    static final String WRONG_SIZE_ERROR_MESSAGE = "file.name.wrong.length.error";

    private static final String LONG_FILE_NAME = "vdJR0YsN0B529zD1rbPS" +
    "gwKNyrpLBpIUYArpdXJMIL5YHwDEP9TgjiiZArjO3dA1LKGrhVp4Rx7YscRI7BuhcfatMpYtZfVL4K16R" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "gwKNyrpLBpIUYArpdXJMIL5YHwDEP9TgjiiZArjO3dA1LKGrhVp4Rx7YscRI7BuhcfatMpYtZfVL4K16R" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "gwKNyrpLBpIUYArpdXJMIL5YHwDEP9TgjiiZArjO3dA1LKGrhVp4Rx7YscRI7BuhcfatMpYtZfVL4K16R" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "B65XA6QmRmnZgLpBfwQBU5ydJz4NGZ9kSSEbZ9QArZgNEKCNHoNifrYP8DvhR9X7DuObumK7XGtANwekC" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "D6ceersXBlJipCrduk7mSVHWAZIrYLCcGKpTuAoB1BlD1DwnFqlNDE1UhtM2uInKGLuyKUsDk3YECxg3v" +
    "QkptWgaV5FC1WFSORdOLkEg3osICQF7Wgxk5p.txt"

    private static final String BAD_FORMATTED_TYPE = "??&^*U&*(&*&*"

    private static final String WELL_FORMATTED_TYPE = "File name.txt"

    @Override
    protected Collection<ValidationCase> getValidationCases() {
        return [
            new UngroupedValidationCase(
            new FileNameValidation(null),
            ExpectedResult.PASSED,
            []
            ),
            new UngroupedValidationCase(
            new FileNameValidation(WELL_FORMATTED_TYPE),
            ExpectedResult.PASSED,
            []
            ),
            new UngroupedValidationCase(
            new FileNameValidation(LONG_FILE_NAME),
            ExpectedResult.FAILED,
            [WRONG_SIZE_ERROR_MESSAGE]
            ),
            new UngroupedValidationCase(
            new FileNameValidation(""),
            ExpectedResult.FAILED,
            [WRONG_SIZE_ERROR_MESSAGE]
            ),
            new UngroupedValidationCase(
            new FileNameValidation(BAD_FORMATTED_TYPE),
            ExpectedResult.FAILED,
            [WRONG_FORMAT_ERROR_MESSAGE]
            ),
        ];
    }
}
