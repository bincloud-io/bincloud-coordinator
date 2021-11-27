package io.bcs.domain.model.validations;

import java.util.Arrays;
import java.util.regex.Pattern;

public class FileNameValidation extends StringValueValidation {
    private static final Pattern PATTERN = Pattern.compile("^[^<>:;,?\\\"*|/]+$");
    
    private final String fileName;

    public FileNameValidation(String fileName) {
        super("file.name", 1L, 255L, Arrays.asList(PATTERN));
        this.fileName = fileName;
    }

    @Override
    protected String getValue() {
        return fileName;
    }
}
