package io.bincloud.storage.port.adapter.file;

import java.sql.Timestamp;
import java.util.Optional;

import javax.persistence.AttributeConverter;

import io.bincloud.common.domain.model.logging.Level;
import io.bincloud.common.domain.model.logging.LogRecord;
import io.bincloud.common.domain.model.logging.Loggers;
import io.bincloud.common.domain.model.message.templates.TextMessageTemplate;
import io.bincloud.common.domain.model.time.DateTime;

public class JPADateTimeConverter implements AttributeConverter<DateTime, Timestamp> {
	@Override
	public Timestamp convertToDatabaseColumn(DateTime attribute) {
		Timestamp converted = Optional.ofNullable(attribute).map(dateTime -> Timestamp.from(dateTime.toInstant()))
				.orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JPA -> JDBC: [{{jpaAttribute}}] -> [{{dbAttribute}}]", attribute, converted)));
		return converted;
	}

	@Override
	public DateTime convertToEntityAttribute(Timestamp dbData) {
		DateTime converted = Optional.ofNullable(dbData).map(timestamp -> new DateTime(timestamp.toInstant()))
				.orElse(null);
		Loggers.applicationLogger(JPADateTimeConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JDBC -> JPA: [{{dbAttribute}}] -> [{{jpaAttribute}}]", converted, dbData)));
		return converted;
	}

	private class LogMessage extends TextMessageTemplate {
		public LogMessage(String textMessage, DateTime jpaAttribute, Timestamp dbAttribute) {
			super(new TextMessageTemplate(textMessage).withParameter("jpaAttribute", jpaAttribute)
					.withParameter("dbAttribute", dbAttribute));
		}

	}
}
