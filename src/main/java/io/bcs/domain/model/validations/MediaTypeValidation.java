package io.bcs.domain.model.validations;

import java.util.Arrays;
import java.util.regex.Pattern;

public class MediaTypeValidation extends StringValueValidation {
    private static final Pattern PATTERN = Pattern.compile("(application|audio|font|example|"
            + "image|message|model|multipart|text|video|x-(?:[0-9A-Za-z!#$%&'*+.^_`|~-]+))/("
            + "[0-9A-Za-z!#$%&'*+.^_`|~-]+)");

    public MediaTypeValidation(String mediaType) {
        super("media.type", 1L, 100L, Arrays.asList(PATTERN));
        this.mediaType = mediaType;
    }

    private final String mediaType;

    @Override
    protected String getValue() {
        return mediaType;
    }
}