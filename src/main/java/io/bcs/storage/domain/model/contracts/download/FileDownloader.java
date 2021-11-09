package io.bcs.storage.domain.model.contracts.download;

import java.util.Collection;
import java.util.Optional;

import io.bce.interaction.pubsub.PubSub;
import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.contracts.FilePointer;

public interface FileDownloader {

	public void downloadFile(FileDownloadRequest fileDownloadRequest, DownloadListener downloadCallback);

	public interface FileDownloadRequest {
		public FilePointer getFile();
		public DownloadRequestDetails getRequestDetails();
		public DestinationPoint getDestinationPoint();
	}
	
	public interface DownloadRequestDetails {
		public Collection<Range> getRanges();
	}
	
	public PubSub<DownloadStage> downloadContent(DownloadFileContent command, BinaryDestination destination);
	
	public interface DownloadFileContent {
		public Optional<FileId> getRevisionName();
		public Collection<Range> getDownloadRanges();
	}
	
	public interface DownloadStage {
		
	}
	
	public interface DownloadCommandHasBeenRejected extends DownloadStage {
		
	}
	
	public interface DataTransmissionHasBeenStarted extends DownloadStage {
		
	}
	
	public interface DataTransmissionHasBeenFailed extends DownloadStage {
		
	}
	
	public interface DataTransmissionHasBeenCompleted extends DownloadStage {
		
	}
	
	public interface FragmentDownloadingHasBeenStarted extends DownloadStage {
		
	}
	
	public interface FragmentDownloadingHasBeenCompleted extends DownloadStage {
		
	}
}
