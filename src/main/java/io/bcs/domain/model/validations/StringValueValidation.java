package io.bcs.domain.model.validations;

import java.util.Collection;
import java.util.regex.Pattern;

import io.bce.validation.ErrorMessage;
import io.bce.validation.Rules;
import io.bce.validation.ValidationContext;
import io.bce.validation.ValidationContext.Validatable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StringValueValidation implements Validatable {

    private final String prefix;
    private final Long minLength;
    private final Long maxLength;
    private final Collection<Pattern> patterns;

    @Override
    public ValidationContext validate(ValidationContext context) {
        ValidationContext result = applySizeRule(context);
        result = applyPatternRules(result);
        return result;
    }

    protected abstract String getValue();

    private ErrorMessage getWrongFormatErrorMessage() {
        return ErrorMessage.createFor(String.format("%s.wrong.format.error", prefix));
    }

    private ErrorMessage getWrongSizeErrorMessage() {
        return ErrorMessage.createFor(String.format("%s.wrong.length.error", prefix));
    }

    private ValidationContext applySizeRule(ValidationContext context) {
        return context.withRule(this::getValue,
                Rules.limitedLength(String.class, minLength, maxLength, getWrongSizeErrorMessage()));
    }

    private ValidationContext applyPatternRules(ValidationContext context) {
        ValidationContext result = context;
        for (Pattern pattern : patterns) {
            result = result.withRule(this::getValue,
                    Rules.pattern(String.class, pattern, getWrongFormatErrorMessage()));
        }
        return result;
    }
}
