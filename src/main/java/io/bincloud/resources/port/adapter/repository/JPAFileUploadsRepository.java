package io.bincloud.resources.port.adapter.repository;

import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.TransactionManager;

import io.bincloud.resources.domain.model.resource.history.UploadedFile;
import io.bincloud.resources.domain.model.resource.history.UploadedFileId;
import io.bincloud.resources.domain.model.resource.history.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileUploadsRepository implements UploadedFileRepository {
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public Optional<UploadedFile> findById(Long resourceId, String fileId) {
		UploadedFileId fileUploadingId = new UploadedFileId(resourceId, fileId);
		return Optional.ofNullable(entityManager.find(UploadedFile.class, fileUploadingId));
	}

	@Override
	public Optional<UploadedFile> findLatestResourceUpload(Long resourceId) {
		TypedQuery<UploadedFile> findLatestResourceUploadingQuery = createFindAllResourceUploadsQuery(resourceId);
		findLatestResourceUploadingQuery.setMaxResults(1);
		return findLatestResourceUploadingQuery.getResultList().stream().findFirst();
	}

	@Override
	@SneakyThrows
	public void save(UploadedFile fileUploading) {
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
	public Stream<UploadedFile> findAllResourceUploads(Long resourceId) {
		return createFindAllResourceUploadsQuery(resourceId).getResultList().stream();
	}
	
	private TypedQuery<UploadedFile> createFindAllResourceUploadsQuery(Long resourceId) {
		TypedQuery<UploadedFile> findLatestResourceUploadingQuery = entityManager
				.createNamedQuery("FileUploadingRepository.findAllResourceUploads", UploadedFile.class);
		findLatestResourceUploadingQuery.setParameter("resourceId", resourceId);
		return findLatestResourceUploadingQuery;
	}
}
