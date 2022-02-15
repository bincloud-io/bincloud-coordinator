package io.bcs.fileserver.infrastructure.repositories.converters;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.AttributeConverter;

/**
 * This class converts {@link LocalDateTime} to {@link Timestamp} and back.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class JpaLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {
  @Override
  public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
    return Optional.ofNullable(attribute).map(Timestamp::valueOf).orElse(null);
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
    return Optional.ofNullable(dbData).map(Timestamp::toLocalDateTime).orElse(null);
  }
}
