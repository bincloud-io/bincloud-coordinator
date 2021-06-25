package io.bincloud.resources.application.download;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.CompletionCallbackWrapper;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.files.domain.model.contracts.FileStorage;
import io.bincloud.resources.domain.model.ResourceRepository;
import io.bincloud.resources.domain.model.contracts.DownloadVisitor;
import io.bincloud.resources.domain.model.contracts.Fragment;
import io.bincloud.resources.domain.model.contracts.Range;
import io.bincloud.resources.domain.model.contracts.RevisionPointer;
import io.bincloud.resources.domain.model.errors.UnsatisfiableRangeFormatException;
import io.bincloud.resources.domain.model.file.FileRevisionDescriptor;
import io.bincloud.resources.domain.model.file.FileUploadsHistory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class FileRevisionAccessor {
	private final FileStorage fileStorage;
	@Getter
	private final FileRevisionDescriptor revisionDescriptor;

	public FileRevisionAccessor(RevisionPointer revisionPointer, FileStorage fileStorage,
			FileUploadsHistory fileUploadHistory, ResourceRepository resourceRepository) {
		super();
		this.fileStorage = fileStorage;
		this.revisionDescriptor = new StoredFileRevisionDescriptor(revisionPointer, fileStorage, fileUploadHistory,
				resourceRepository);
	}
	
	public DownloadOperation doFwnload(DestinationPoint destinationPoint, DownloadVisitor downloadVisitor) {
		return this.download(Arrays.asList(new NullRange()), destinationPoint, downloadVisitor);
	}
	
	public DownloadOperation download(Collection<Range> ranges, DestinationPoint destinationPoint,
			DownloadVisitor downloadVisitor) {
		DownloadOperation downloadOperation = buildDownloadOperation(ranges, destinationPoint, downloadVisitor);
		return () -> {
			try {
				downloadOperation.downloadFile();
			} catch (Exception error) {
				downloadVisitor.onError(revisionDescriptor, error);
			}
		};
	}

	private DownloadOperation buildDownloadOperation(Collection<Range> ranges, DestinationPoint destinationPoint,
			DownloadVisitor downloadVisitor) {
		return () -> {
			downloadVisitor.onStart(revisionDescriptor);
			checkRangesCount(ranges);
			createFragments(ranges)
					.<DownloadProcessStepCreator>map(
							fragment -> new FragmentDownloaderCreator(fragment, destinationPoint, downloadVisitor))
					.reduce(createDownloadCompleteStep(downloadVisitor), (result, current) -> {
						return ((FragmentDownloaderCreator) current).chain(result.createStep());
					}).createStep().downloadFile();
		};
	}

	private void checkRangesCount(Collection<Range> ranges) {
		if (ranges.isEmpty()) {
			throw new UnsatisfiableRangeFormatException();
		}
	}

	private DownloadProcessStepCreator createDownloadCompleteStep(DownloadVisitor downloadVisitor) {
		return () -> {
			return () -> downloadVisitor.onEnd(revisionDescriptor);
		};
	}

	private Stream<Fragment> createFragments(Collection<Range> ranges) {
		return ranges.stream().map(range -> new StoredFileFragment(range, getFileSize()));
	}

	private Long getFileSize() {
		return revisionDescriptor.getFileSize();
	}

	private String getFileId() {
		return revisionDescriptor.getFileId();
	}

	private interface DownloadProcessStepCreator {
		public DownloadOperation createStep();
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private class FragmentDownloaderCreator implements DownloadProcessStepCreator {
		private final Fragment fragment;
		private final DestinationPoint destinationPoint;
		private final CompletionCallback callback;

		public FragmentDownloaderCreator(Fragment fragment, DestinationPoint destinationPoint,
				DownloadVisitor downloadVisitor) {
			this(fragment, destinationPoint, new FragmentCallback(revisionDescriptor, downloadVisitor, fragment));
		}

		public DownloadOperation createStep() {
			return () -> {
				fileStorage.downloadFileRange(getFileId(), destinationPoint, callback, getStart(), getSize());
			};
		}

		public FragmentDownloaderCreator chain(DownloadOperation fragmentDownloader) {
			return new FragmentDownloaderCreator(fragment, destinationPoint, new CompletionCallbackWrapper(callback) {
				@Override
				public void onSuccess() {
					super.onSuccess();
					fragmentDownloader.downloadFile();
				}
			});
		}

		private Long getStart() {
			return fragment.getStart();
		}

		private Long getSize() {
			return fragment.getSize();
		}
	}
}
