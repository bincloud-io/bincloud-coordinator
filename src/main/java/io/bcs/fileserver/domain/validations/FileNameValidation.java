package io.bcs.fileserver.domain.validations;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * This validation checks the file name format.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FileNameValidation extends StringValueValidation {
  private static final Pattern PATTERN = Pattern.compile("^[^<>:;,?\\\"*|/]+$");

  private final String fileName;

  public FileNameValidation(String fileName) {
    super("file.name", 1L, 400L, Arrays.asList(PATTERN));
    this.fileName = fileName;
  }

  @Override
  protected String getValue() {
    return fileName;
  }
}
