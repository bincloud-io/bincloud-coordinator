package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bce.interaction.streaming.binary.BinarySource;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bcs.fileserver.domain.model.content.ContentLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is responsible for physical file management, stored on filesystem.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class FilesystemPhysicalFile implements PhysicalFile {
  private final File file;

  private FilesystemPhysicalFile(String baseDirectory, ContentLocator contentLocator) {
    super();
    this.file = new File(getFileLocationPath(baseDirectory, contentLocator));
  }

  public static Factory factory() {
    return FilesystemPhysicalFile::new;
  }

  @Override
  public void create() throws IOException {
    file.createNewFile();
  }

  @Override
  public BinarySource openForRead(Long offset, Long limit, Integer bufferSize) throws IOException {
    FileInputStream stream = new FileInputStream(file);
    stream.skip(offset);
    return new InputStreamSource(stream, limit, bufferSize);
  }

  @Override
  public BinaryDestination openForWrite() throws IOException {
    FileOutputStream stream = new FileOutputStream(file);
    return new OutputStreamDestination(stream);
  }

  @Override
  public void delete() {
    file.delete();
  }

  private String getFileLocationPath(String baseDirectory, ContentLocator contentLocator) {
    return String.format("%s/%s", baseDirectory, contentLocator.getStorageFileName());
  }
}
