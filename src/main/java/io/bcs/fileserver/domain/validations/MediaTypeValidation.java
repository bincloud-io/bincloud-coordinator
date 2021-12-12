package io.bcs.fileserver.domain.validations;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This validation checks the mediatype format acceptance.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class MediaTypeValidation extends StringValueValidation {
  private static final Pattern PATTERN = Pattern.compile("(application|audio|font|example|"
      + "image|message|model|multipart|text|video|x-(?:[0-9A-Za-z!#$%&'*+.^_`|~-]+))/("
      + "[0-9A-Za-z!#$%&'*+.^_`|~-]+)");

  public MediaTypeValidation(String mediaType) {
    super("media.type", 1L, 200L, Arrays.asList(PATTERN));
    this.mediaType = mediaType;
  }

  private final String mediaType;

  @Override
  protected String getValue() {
    return mediaType;
  }
}
