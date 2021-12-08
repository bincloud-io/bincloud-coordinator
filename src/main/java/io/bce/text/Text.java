package io.bce.text;

import javax.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

/**
 * This class is the text processing global access mechanism. It includes text processor registry
 * and helper methods, allow to use this mechanism independently of architectural layer.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@UtilityClass
public class Text {
  private TextProcessor textProcessor;

  static {
    reset();
  }

  /**
   * Get the text processor.
   *
   * @return text processor
   */
  public final TextProcessor processor() {
    return textProcessor;
  }

  /**
   * Interpolate text template.
   *
   * @param textTemplate The text template
   * @return The interpolated text
   */
  public final String interpolate(TextTemplate textTemplate) {
    return processor().interpolate(textTemplate);
  }

  /**
   * Configure the processor.
   *
   * @param textProcessor The text processor
   */
  public final void configureProcessor(@NotNull TextProcessor textProcessor) {
    Text.textProcessor = textProcessor;
  }

  /**
   * Reset the text processor configuration.
   */
  public void reset() {
    configureProcessor(TextProcessor.create());
  }
}