package io.bcs.storage.port.adapter.file.repository;

import java.util.Arrays;
import java.util.Optional;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import io.bcs.storage.domain.model.states.FileRevisionStatus;

@Converter(autoApply = false)
public class JPAFileRevisionStateConverter implements AttributeConverter<FileRevisionState, String> {
	@Override
	public String convertToDatabaseColumn(FileRevisionState attribute) {
		String dbColumn = Optional.ofNullable(attribute).map(state -> state.getStatus()).orElse(null);
		Loggers.applicationLogger(JPAFileRevisionStateConverter.class).log(new LogRecord(Level.TRACE,
				logMessageText("Convert JPA -> JDBC: [{{jpaAttribute}}] -> [{{dbColumn}}]", attribute, dbColumn)));
		return dbColumn;
	}

	@Override
	public FileRevisionState convertToEntityAttribute(String dbData) {
		FileRevisionState fileState = Optional.ofNullable(dbData).map(status -> extractFileState(status)).orElse(null);
		Loggers.applicationLogger(JPAFileRevisionStateConverter.class).log(new LogRecord(Level.TRACE,
				logMessageText("Convert JDBC -> JPA: [{{dbColumn}}] -> [{{jpaAttribute}}]", fileState, dbData)));
		return fileState;
	}

	private FileRevisionState extractFileState(String statusName) {
		return Arrays.stream(FileRevisionStatus.values()).filter(fileStatus -> statusName.equals(fileStatus.toString()))
				.findFirst().map(FileRevisionStatus::getFileState).orElseThrow(() -> new Error("Must never be happened."));
	}

	private TextTemplate logMessageText(String textMessage, FileRevisionState attribute, String dbColumn) {
		return TextTemplates.createBy(textMessage).withParameter("jpaAttribute", attribute).withParameter("dbColumn",
				dbColumn);
	}
}
