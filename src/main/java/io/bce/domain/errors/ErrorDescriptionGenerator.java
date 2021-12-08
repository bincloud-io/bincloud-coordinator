package io.bce.domain.errors;

import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.text.TextProcessor;
import io.bce.text.TextTemplate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for error description generation for specified error descriptor using
 * text processing mechanism.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ErrorDescriptionGenerator {
  private final ErrorDescriptor applicationError;
  private final TextProcessor textProcessor;

  /**
   * Generate description.
   *
   * @return The human-readable text description about happened error
   */
  public String generateDescription() {
    final TextTemplate textTemplate = ErrorDescriptorTemplate.createFor(applicationError);
    return Optional.of(textTemplate).map(template -> textProcessor.interpolate(template))
        .filter(message -> !message.equals(textTemplate.getTemplateText()))
        .orElse(applicationError.toString());
  }

  /**
   * Create the error description generator.
   *
   * @param textProcessor   The text processor
   * @param errorDescriptor The error descriptor
   * @return The error description generator
   */
  public static ErrorDescriptionGenerator of(TextProcessor textProcessor,
      ErrorDescriptor errorDescriptor) {
    return new ErrorDescriptionGenerator(errorDescriptor, textProcessor);
  }
}
