package io.bcs.storage.port.adapter.file.repository;

import java.util.Arrays;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.common.domain.model.message.templates.TextMessageTemplate;
import io.bcs.storage.domain.model.FileState;
import io.bcs.storage.domain.model.states.FileStatus;

@Converter(autoApply = false)
public class JPAFileStateConverter implements AttributeConverter<FileState, String> {
	@Override
	public String convertToDatabaseColumn(FileState attribute) {
		String dbColumn = Optional.ofNullable(attribute).map(state -> state.getStatus()).orElse(null);
		Loggers.applicationLogger(JPAFileStateConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JPA -> JDBC: [{{jpaAttribute}}] -> [{{dbColumn}}]", attribute, dbColumn)));
		return dbColumn;
	}

	@Override
	public FileState convertToEntityAttribute(String dbData) {
		FileState fileState = Optional.ofNullable(dbData).map(status -> extractFileState(status)).orElse(null);
		Loggers.applicationLogger(JPAFileStateConverter.class).log(new LogRecord(Level.TRACE,
				new LogMessage("Convert JDBC -> JPA: [{{dbColumn}}] -> [{{jpaAttribute}}]", fileState, dbData)));
		return fileState;
	}

	private FileState extractFileState(String statusName) {
		return Arrays.stream(FileStatus.values()).filter(fileStatus -> statusName.equals(fileStatus.toString()))
				.findFirst().map(FileStatus::getFileState).orElseThrow(() -> new Error("Must never be happened."));
	}

	private class LogMessage extends TextMessageTemplate {
		public LogMessage(String textMessage, FileState attribute, String dbColumn) {
			super(new TextMessageTemplate(textMessage).withParameter("jpaAttribute", attribute)
					.withParameter("dbColumn", dbColumn));
		}
	}
}
