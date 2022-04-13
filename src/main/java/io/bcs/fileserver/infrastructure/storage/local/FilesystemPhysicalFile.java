package io.bcs.fileserver.infrastructure.storage.local;

import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bce.interaction.streaming.binary.BinarySource;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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
    if (!file.createNewFile()) {      
      truncate();
    }
  }

  @Override
  public void truncate() throws IOException {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
      randomAccessFile.setLength(0);
    }
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
  public void delete() throws IOException {
    file.delete();
  }

  private String getFileLocationPath(String baseDirectory, ContentLocator contentLocator) {
    return String.format("%s/%s", baseDirectory, contentLocator.getStorageFileName());
  }
}
