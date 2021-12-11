package io.bcs.domain.model.validations;

import io.bce.validation.ErrorMessage;
import io.bce.validation.ValidationContext.Rule;
import io.bcs.DictionaryValidation;

public class MediaTypeAcceptanceValidation extends DictionaryValidation<String> {
  public static final String RULE_ALIAS = MediaTypeAcceptanceValidation.class.getName();
  public static final String INACCEPTABLE_MEDIA_TYPE_MESSAGE = "media.type.not.acceptable";

  public MediaTypeAcceptanceValidation(String validatableValue) {
    super(RULE_ALIAS, validatableValue);
  }

  public static Rule<String> createGlobalRule(DictionaryPredicate<String> predicate) {
    return createGlobalRule(String.class, ErrorMessage.createFor(INACCEPTABLE_MEDIA_TYPE_MESSAGE),
        predicate);
  }
}
