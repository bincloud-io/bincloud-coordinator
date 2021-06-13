package io.bincloud.storage.port.adapter.repository;

import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.TransactionManager;

import io.bincloud.storage.domain.model.file.FileUpload;
import io.bincloud.storage.domain.model.file.FileUploadId;
import io.bincloud.storage.domain.model.file.FileUploadsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileUploadsRepository implements FileUploadsRepository {
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public Optional<FileUpload> findById(Long resourceId, String fileId) {
		FileUploadId fileUploadingId = new FileUploadId(resourceId, fileId);
		return Optional.ofNullable(entityManager.find(FileUpload.class, fileUploadingId));
	}

	@Override
	public Optional<FileUpload> findLatestResourceUpload(Long resourceId) {
		TypedQuery<FileUpload> findLatestResourceUploadingQuery = createFindAllResourceUploadsQuery(resourceId);
		findLatestResourceUploadingQuery.setMaxResults(1);
		return findLatestResourceUploadingQuery.getResultList().stream().findFirst();
	}

	@Override
	@SneakyThrows
	public void save(FileUpload fileUploading) {
		transactionManager.begin();
		entityManager.merge(fileUploading);
		transactionManager.commit();
	}

	@Override
	@SneakyThrows
	public void remove(Long resourceId, String fileId) {
		transactionManager.begin();
		findById(resourceId, fileId).map(entityManager::merge).ifPresent(entityManager::remove);
		transactionManager.commit();
	}

	@Override
	public Stream<FileUpload> findAllResourceUploads(Long resourceId) {
		return createFindAllResourceUploadsQuery(resourceId).getResultList().stream();
	}
	
	private TypedQuery<FileUpload> createFindAllResourceUploadsQuery(Long resourceId) {
		TypedQuery<FileUpload> findLatestResourceUploadingQuery = entityManager
				.createNamedQuery("FileUploadingRepository.findAllResourceUploads", FileUpload.class);
		findLatestResourceUploadingQuery.setParameter("resourceId", resourceId);
		return findLatestResourceUploadingQuery;
	}
}
