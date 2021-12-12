package io.bcs.fileserver.infrastructure.storage;

import io.bce.Generator;
import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file storage, which stores.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class LocalFileSystemStorage implements FileStorage {
  private final Generator<String> fileNameGenerator;
  private final String storageName;
  private final String baseDirectory;
  private final int bufferSize;

  @Override
  public ContentLocator create(String mediaType) throws FileStorageException {
    try {
      ContentLocator contentLocator = new FileLocator();
      getLocatedFile(contentLocator).createNewFile();
      return contentLocator;
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException {
    try {
      checkThatLocatedContentIsRelatedToTheCurrentStorage(contentLocator);
      return new OutputStreamDestination(openFileForWrite(contentLocator));
    } catch (FileNotFoundException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator,
      ContentFragment fragment) throws FileStorageException {
    try {
      checkThatLocatedContentIsRelatedToTheCurrentStorage(contentLocator);
      FileInputStream inputStream = openFileForRead(contentLocator);
      inputStream.skip(fragment.getOffset());
      return new InputStreamSource(inputStream, fragment.getLength(), bufferSize);
    } catch (IOException error) {
      throw new FileStorageException(error);
    }
  }

  @Override
  public void delete(ContentLocator contentLocator) throws FileStorageException {
    checkThatLocatedContentIsRelatedToTheCurrentStorage(contentLocator);
    getLocatedFile(contentLocator).delete();
  }

  private FileOutputStream openFileForWrite(ContentLocator contentLocator)
      throws FileNotFoundException {
    return new FileOutputStream(getLocatedFile(contentLocator));
  }

  private FileInputStream openFileForRead(ContentLocator contentLocator)
      throws FileNotFoundException {
    return new FileInputStream(getFileLocationPath(contentLocator));
  }

  private void checkThatLocatedContentIsRelatedToTheCurrentStorage(ContentLocator contentLocator) {
    if (!contentLocator.getStorageName().equals(storageName)) {
      throw new FileStorageException(new UnrelatedFileStorageException(contentLocator));
    }
  }

  private File getLocatedFile(ContentLocator contentLocator) {
    return new File(getFileLocationPath(contentLocator));
  }

  private String getFileLocationPath(ContentLocator contentLocator) {
    return String.format("%s/%s", baseDirectory, contentLocator.getStorageFileName());
  }

  private final class FileLocator implements ContentLocator {
    @Getter
    private final String storageFileName;

    public FileLocator() {
      super();
      this.storageFileName = fileNameGenerator.generateNext();
    }

    @Override
    public String getStorageName() {
      return storageName;
    }
  }

  private final class UnrelatedFileStorageException extends Exception {
    private static final long serialVersionUID = -2401682394723841722L;

    public UnrelatedFileStorageException(ContentLocator contentLocator) {
      super(String.format("The current file storage [%s] is not the related file storage [%s]",
          storageName, contentLocator.getStorageName()));
    }
  }
}
