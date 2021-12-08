package io.bce.validation;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class represents the validation group name value and it is responsible for manipulations
 * with this. You should wrap your string values by this type so this class . The validation group
 * name length must not exceed 1000 characters.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ValidationGroup {
  private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("\\S+");
  public static final ValidationGroup UNGROUPED = new ValidationGroup("$$__UNGROUPED_MESSAGES__$$");

  @NonNull
  private final String value;

  /**
   * Create the validation group for specified name.
   *
   * @param name The validation group name
   * @return The validation group
   */
  public static final ValidationGroup createFor(String name) {
    checkThatGroupNameIsMatchedToPattern(name);
    return new ValidationGroup(name);
  }

  private static final void checkThatGroupNameIsMatchedToPattern(String name) {
    if (!GROUP_NAME_PATTERN.matcher(name).matches()) {
      throw new WrongValidationGroupFormatException(name);
    }
  }

  @Override
  public String toString() {
    return value;
  }

  public final ValidationGroup deriveWith(ValidationGroup subGroup) {
    if (subGroup.isReservedGroup()) {
      return this;
    }

    if (this.isReservedGroup()) {
      return subGroup;
    }

    return new ValidationGroup(String.format("%s.%s", this, subGroup));
  }

  private boolean isReservedGroup() {
    return isUngrouped();
  }

  private boolean isUngrouped() {
    return this == UNGROUPED;
  }

  public static final class WrongValidationGroupFormatException extends RuntimeException {
    private static final long serialVersionUID = 5777870239396831456L;

    public WrongValidationGroupFormatException(String groupName) {
      super(String.format("The validation group name \"%s\" isn't matched to the \"%s\" pattern.",
          groupName, GROUP_NAME_PATTERN));
    }
  }
}
