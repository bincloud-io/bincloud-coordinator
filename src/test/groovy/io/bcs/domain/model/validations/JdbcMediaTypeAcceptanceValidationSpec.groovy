package io.bcs.domain.model.validations

import io.bce.validation.GlobalValidations
import io.bce.validation.UngroupedValidationCase
import io.bce.validation.ValidationCase
import io.bce.validation.ValidationCase.ExpectedResult
import io.bce.validation.ValidationSpecification
import io.bcs.DictionaryValidation.DictionaryPredicate

class JdbcMediaTypeAcceptanceValidationSpec extends ValidationSpecification {
    private static final String PASSED_PARAMETER = "application/well-type"
    private static final String FAILED_PARAMETER = "application/bad-type"
    private DictionaryPredicate acceptancePredicate;

    def setup() {
        acceptancePredicate = Mock(DictionaryPredicate)
        GlobalValidations.registerRule(MediaTypeAcceptanceValidation.RULE_ALIAS, MediaTypeAcceptanceValidation.createGlobalRule(acceptancePredicate))
        acceptancePredicate.isSatisfiedBy(PASSED_PARAMETER) >> true
        acceptancePredicate.isSatisfiedBy(FAILED_PARAMETER) >> false
    }

    @Override
    protected Collection<ValidationCase> getValidationCases() {
        return [
            new UngroupedValidationCase(
            new MediaTypeAcceptanceValidation(PASSED_PARAMETER),
            ExpectedResult.PASSED,
            []
            ),
            new UngroupedValidationCase(
            new MediaTypeAcceptanceValidation(FAILED_PARAMETER),
            ExpectedResult.FAILED,
            [
                MediaTypeAcceptanceValidation.INACCEPTABLE_MEDIA_TYPE_MESSAGE
            ]
            )
        ];
    }

    def cleanup() {
        GlobalValidations.clear()
    }
}
