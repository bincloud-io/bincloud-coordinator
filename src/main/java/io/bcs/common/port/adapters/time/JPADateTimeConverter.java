package io.bcs.common.port.adapters.time;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;

@Converter(autoApply = false)
public class JPADateTimeConverter implements AttributeConverter<Instant, Timestamp> {
	@Override
	public Timestamp convertToDatabaseColumn(Instant attribute) {
		Timestamp converted = Optional.ofNullable(attribute).map(Timestamp::from).orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				logMessageText("Convert JPA -> JDBC: [{{jpaAttribute}}] -> [{{dbAttribute}}]", attribute, converted)));
		return converted;
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp dbData) {
		Instant converted = Optional.ofNullable(dbData).map(Timestamp::toInstant).orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				logMessageText("Convert JDBC -> JPA: [{{dbAttribute}}] -> [{{jpaAttribute}}]", converted, dbData)));
		return converted;
	}

	private TextTemplate logMessageText(String textMessage, Instant attribute, Timestamp dbColumn) {
		return TextTemplates.createBy(textMessage).withParameter("jpaAttribute", attribute).withParameter("dbAttribute",
				dbColumn);
	}
}
