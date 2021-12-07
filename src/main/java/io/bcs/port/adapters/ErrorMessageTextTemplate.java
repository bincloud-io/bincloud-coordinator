package io.bcs.port.adapters;

import java.util.Map;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bce.validation.ErrorMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorMessageTextTemplate implements TextTemplate {
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
