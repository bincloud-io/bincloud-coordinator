package io.bincloud.files.port.adapter.file.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import io.bincloud.common.domain.model.logging.Level;
import io.bincloud.common.domain.model.logging.LogRecord;
import io.bincloud.common.domain.model.logging.Loggers;
import io.bincloud.common.domain.model.message.templates.StringifiedObjectTemplate;
import io.bincloud.common.domain.model.message.templates.TextMessageTemplate;
import io.bincloud.files.domain.model.File;
import io.bincloud.files.domain.model.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileRepository implements FileRepository {
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public Optional<File> findById(String fileId) {
		Loggers.applicationLogger(JPAFileRepository.class).log(new LogRecord(Level.TRACE,
				new TextMessageTemplate("Find file with id={{fileId}}").withParameter("fileId", fileId)));
		return Optional.ofNullable(entityManager.find(File.class, fileId));
	}

	@Override
	@SneakyThrows
	public void save(File file) {
		Loggers.applicationLogger(JPAFileRepository.class)
				.log(new LogRecord(Level.TRACE, new TextMessageTemplate("Save file: {{file}}").withParameter("file",
						new StringifiedObjectTemplate(file))));
		transactionManager.begin();
		this.entityManager.merge(file);
		transactionManager.commit();
	}
}
