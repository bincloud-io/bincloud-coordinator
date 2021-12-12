package io.bcs.fileserver.domain.services;

import io.bce.logging.ApplicationLogger;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class FileProvider implements Supplier<File> {
  private final String storageFileName;
  private final FileRepository fileRepository;
  private final ApplicationLogger logger;

  @Override
  public File get() {
    return fileRepository.findById(storageFileName).orElseThrow(() -> {
      logger.warn(TextTemplates.createBy("The file with {{storageFileName}} hasn't been found.")
          .withParameter("storageFileName", storageFileName));
      return new FileNotExistsException();
    });
  }
}
