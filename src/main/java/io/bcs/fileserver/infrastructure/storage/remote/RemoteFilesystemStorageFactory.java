package io.bcs.fileserver.infrastructure.storage.remote;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bcs.fileserver.api.services.internal.BCSPrivateServicesEndpoint;
import io.bcs.fileserver.api.services.internal.WSRemoteStorage;
import io.bcs.fileserver.api.types.internal.ContentDirectAccessUrlRsType;
import io.bcs.fileserver.api.types.internal.CreateFileRqType;
import io.bcs.fileserver.api.types.internal.CreateFileRsType;
import io.bcs.fileserver.api.types.internal.DeleteFileRqType;
import io.bcs.fileserver.api.types.internal.FileDescriptorType;
import io.bcs.fileserver.api.types.internal.GetDirectContentReadUrlRqType;
import io.bcs.fileserver.api.types.internal.GetDirectContentWriteUrlRqType;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.FileStorage.FileDescriptor;
import io.bcs.fileserver.domain.model.storage.FileStorageProvider;
import io.bcs.fileserver.domain.model.storage.types.RemoteStorageDescriptor;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.types.ContentFragmentType;
import io.bcs.fileserver.types.ContentLocatorType;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class implements the file storage, interacting with the remote filesystem.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class RemoteFilesystemStorageFactory
    implements FileStorageProvider<RemoteStorageDescriptor> {

  private final FileServerConfigurationProperties fileServerConfig;

  @Override
  public FileStorage getFileStorage(RemoteStorageDescriptor storage) {
    BCSPrivateServicesEndpoint endpoint =
        new BCSPrivateServicesEndpoint(storage.getRemoteStorageGatewayWsdl());
    return new RemoteFileStorage(endpoint.getWSRemoteStoragePort());
  }

  @RequiredArgsConstructor
  private class RemoteFileStorage implements FileStorage {
    private final WSRemoteStorage remoteStorage;

    @Override
    public ContentLocator create(FileDescriptor file, Long contentLength)
        throws FileStorageException {
      try {
        CreateFileRsType response =
            remoteStorage.createFile(new WsCreateFileCommand(file, contentLength));
        return new WsContentLocator(response.getContentLocator());
      } catch (Exception error) {
        throw new FileStorageException(error);
      }
    }

    @Override
    public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
        throws FileStorageException {
      try {
        ContentDirectAccessUrlRsType response =
            remoteStorage.getContentWriteUrl(new WsGetWriteUrlCommand(contentLocator));
        WsFileConnection connection = new WsFileConnection(new URL(response.getContentAccessUrl()));
        return connection.getDestination();
      } catch (Exception error) {
        throw new FileStorageException(error);
      }
    }

    @Override
    public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
        ContentFragment fragment) throws FileStorageException {
      try {
        ContentDirectAccessUrlRsType response =
            remoteStorage.getContentReadUrl(new WsGetReadUrlCommand(contentLocator, fragment));
        WsFileConnection connection = new WsFileConnection(new URL(response.getContentAccessUrl()));
        return connection.getSource();
      } catch (Exception error) {
        throw new FileStorageException(error);
      }
    }

    @Override
    public void delete(FileDescriptor file) throws FileStorageException {
      try {
        remoteStorage.deleteFile(new WsDeleteFileCommand(file));
      } catch (Exception error) {
        throw new FileStorageException(error);
      }
    }
  }

  private static class WsGetReadUrlCommand extends GetDirectContentReadUrlRqType {
    WsGetReadUrlCommand(ContentLocator contentLocator, ContentFragment fragment) {
      super();
      setLocator(new WsContentLocatorInfo(contentLocator));
      setFragment(new WsFragmentInfo(fragment));
    }
  }

  private static class WsGetWriteUrlCommand extends GetDirectContentWriteUrlRqType {
    WsGetWriteUrlCommand(ContentLocator contentLocator) {
      super();
      setContentLocator(new WsContentLocatorInfo(contentLocator));
    }
  }

  private static class WsFragmentInfo extends ContentFragmentType {
    WsFragmentInfo(ContentFragment contentFragment) {
      super();
      setOffset(contentFragment.getOffset());
      setLength(contentFragment.getLength());
    }
  }

  private static class WsContentLocatorInfo extends ContentLocatorType {
    WsContentLocatorInfo(ContentLocator contentLocator) {
      super();
      setStorageName(contentLocator.getStorageName());
      setStorageFileName(contentLocator.getStorageFileName());
    }
  }

  private class WsFileConnection {
    private final HttpsURLConnection urlConnection;

    @SneakyThrows
    WsFileConnection(URL url) {
      super();
      this.urlConnection = (HttpsURLConnection) url.openConnection();
    }

    @SneakyThrows
    public Source<BinaryChunk> getSource() {
      urlConnection.setRequestMethod("GET");
      urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
      return new InputStreamSource(urlConnection.getInputStream(),
          fileServerConfig.getBufferSize());
    }

    @SneakyThrows
    public Destination<BinaryChunk> getDestination() {
      urlConnection.setRequestMethod("POST");
      urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
      return new OutputStreamDestination(urlConnection.getOutputStream());
    }
  }

  private static class WsCreateFileCommand extends CreateFileRqType {
    WsCreateFileCommand(FileDescriptor fileDescriptor, Long contentLength) {
      super();
      setFile(new WsFileDescriptor(fileDescriptor));
      setContentLength(contentLength);
    }
  }

  private static class WsDeleteFileCommand extends DeleteFileRqType {
    WsDeleteFileCommand(FileDescriptor fileDescriptor) {
      super();
      setFile(new WsFileDescriptor(fileDescriptor));
    }
  }

  private static class WsContentLocator extends DefaultContentLocator {
    public WsContentLocator(ContentLocatorType locatorInfo) {
      super(locatorInfo.getStorageName(), locatorInfo.getStorageFileName());
    }
  }

  private static class WsFileDescriptor extends FileDescriptorType {
    WsFileDescriptor(FileStorage.FileDescriptor fileDescriptor) {
      super();
      setStorageName(fileDescriptor.getStorageName().get());
      setStorageFileName(fileDescriptor.getStorageFileName());
      setMediaType(fileDescriptor.getMediaType());
    }
  }
}
