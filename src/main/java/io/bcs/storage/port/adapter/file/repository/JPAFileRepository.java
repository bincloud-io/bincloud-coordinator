package io.bcs.storage.port.adapter.file.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.common.domain.model.message.templates.StringifiedObjectTemplate;
import io.bcs.common.domain.model.message.templates.TextMessageTemplate;
import io.bcs.storage.domain.model.File;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileRepository implements FileRepository {
//	private static final String FIND_LATEST_RESOURCE_UPLOAD_NAMED_QUERY = "FileRepository.findLatestResourceUpload";
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public Optional<File> findById(String fileId) {
		Loggers.applicationLogger(JPAFileRepository.class)
				.log(new LogRecord(Level.TRACE,
						new TextMessageTemplate("Find file with fileId={{fileId}}")
								.withParameter("fileId", fileId)));
		return Optional.ofNullable(entityManager.find(File.class, new FileId(fileId)));
	}

//	@Override
//	public Optional<File> findLatestResourceUpload(Long resourceId) {
//		Loggers.applicationLogger(JPAFileRepository.class).log(
//				new LogRecord(Level.TRACE, new TextMessageTemplate("Find latest file with resourceId={{resourceId}}")
//						.withParameter("resourceId", resourceId)));
//		TypedQuery<File> findLatestFileQuery = entityManager.createNamedQuery(FIND_LATEST_RESOURCE_UPLOAD_NAMED_QUERY, File.class);
//		findLatestFileQuery.setParameter("resourceId", resourceId);
//		return Optional.ofNullable(findLatestFileQuery.getSingleResult());
//	}

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
