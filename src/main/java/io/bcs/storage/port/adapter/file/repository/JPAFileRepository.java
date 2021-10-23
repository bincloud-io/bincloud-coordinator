package io.bcs.storage.port.adapter.file.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.common.domain.model.logging.Level;
import io.bcs.common.domain.model.logging.LogRecord;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.storage.domain.model.FileRevision;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.FileRevisionRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileRepository implements FileRevisionRepository {
//	private static final String FIND_LATEST_RESOURCE_UPLOAD_NAMED_QUERY = "FileRepository.findLatestResourceUpload";
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;
	
	@Override
	public Optional<FileRevision> findById(FileId fileId) {
		Loggers.applicationLogger(JPAFileRepository.class).log(new LogRecord(Level.TRACE,
				TextTemplates.createBy("Find file with fileId={{fileId}}").withParameter("fileId", fileId)));

		return Optional.ofNullable(entityManager.find(FileRevision.class, fileId));
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
	public void save(FileRevision file) {
		Loggers.applicationLogger(JPAFileRepository.class).log(new LogRecord(Level.TRACE,
				TextTemplates.createBy("Save file: {{file}}").withParameter("file", TextTemplates.createBy(file))));
		transactionManager.begin();
		this.entityManager.merge(file);
		transactionManager.commit();
	}
}
