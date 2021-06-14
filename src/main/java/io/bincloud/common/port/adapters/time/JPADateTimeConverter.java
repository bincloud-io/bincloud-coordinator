package io.bincloud.common.port.adapters.time;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.bincloud.common.domain.model.logging.Level;
import io.bincloud.common.domain.model.logging.LogRecord;
import io.bincloud.common.domain.model.logging.Loggers;
import io.bincloud.common.domain.model.message.templates.TextMessageTemplate;

@Converter(autoApply = false)
public class JPADateTimeConverter implements AttributeConverter<Instant, Timestamp> {
	@Override
	public Timestamp convertToDatabaseColumn(Instant attribute) {
		Timestamp converted = Optional.ofNullable(attribute).map(Timestamp::from).orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JPA -> JDBC: [{{jpaAttribute}}] -> [{{dbAttribute}}]", attribute, converted)));
		return converted;
	}

	@Override
	public Instant convertToEntityAttribute(Timestamp dbData) {
		Instant converted = Optional.ofNullable(dbData).map(Timestamp::toInstant).orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JDBC -> JPA: [{{dbAttribute}}] -> [{{jpaAttribute}}]", converted, dbData)));
		return converted;
	}

	private class LogMessage extends TextMessageTemplate {
		public LogMessage(String textMessage, Instant jpaAttribute, Timestamp dbAttribute) {
			super(new TextMessageTemplate(textMessage).withParameter("jpaAttribute", jpaAttribute)
					.withParameter("dbAttribute", dbAttribute));
		}

	}
}
