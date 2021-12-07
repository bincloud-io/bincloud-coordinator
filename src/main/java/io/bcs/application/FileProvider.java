package io.bcs.application;

import java.util.function.Supplier;

import io.bce.logging.ApplicationLogger;
import io.bce.text.TextTemplates;
import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.FileNotExistsException;
import io.bcs.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileProvider implements Supplier<File> {

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
