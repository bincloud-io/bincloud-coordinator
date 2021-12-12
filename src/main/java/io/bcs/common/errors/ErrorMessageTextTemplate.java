package io.bcs.common.errors;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bce.validation.ErrorMessage;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ErrorMessageTextTemplate implements TextTemplate {
  private final ErrorMessage errorMessage;

  @Override
  public String getTemplateText() {
    return errorMessage.getMessage();
  }

  @Override
  public Map<String, Object> getParameters() {
    return errorMessage.getParameters();
  }

  @Override
  public TextTemplate transformBy(Transformer transformer) {
    return TextTemplates.createBy(this).transformBy(transformer);
  }
}
