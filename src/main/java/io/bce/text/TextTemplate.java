package io.bce.text;

import java.util.Map;

/**
 * This interface describes the text template.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface TextTemplate {
  /**
   * Get the non-compiled string template text.
   *
   * @return The not-null string template text
   */
  public String getTemplateText();

  /**
   * Get the string template parameters.
   *
   * @return The template parameters map interpolation
   */
  public Map<String, Object> getParameters();

  /**
   * Get the hash code of the text template.
   *
   * @return The hash code
   */
  @Override
  int hashCode();

  /**
   * Check equivalence with another object. It is important if two objects are equal, their
   * {@link TextTemplate#hashCode()} results should be equal(by convention) and
   * {@link TextTemplate#toString()} results should be equal and have equal hash codes.
   *
   * @param opposite The opposite object
   * @return True if they are equal and false otherwise
   */
  @Override
  boolean equals(Object opposite);

  /**
   * Compile template and return result according to chosen interpolation algorithm which the
   * template string implementation applies for interpolation.
   *
   * @return The template compilation result
   */
  @Override
  public String toString();

  /**
   * Transform text by the specified transformer.
   *
   * @param transformer The text message transformer
   * @return The transformed message
   */
  public TextTemplate transformBy(Transformer transformer);

  /**
   * This interface describes the contract of the text template transformation.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface Transformer {
    /**
     * Transform the source text template to the derived text template.
     *
     * @param sourceTemplate The source template which is going to be transformed
     * @return The result transformed template
     */
    public TextTemplate transform(TextTemplate sourceTemplate);
  }
}
