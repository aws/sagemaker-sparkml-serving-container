package com.amazonaws.sagemaker.type;

import com.google.common.collect.ImmutableMap;
import org.springframework.util.MimeType;

public final class AdditionalMimeType {

    public static final MimeType TEXT_CSV = new MimeType("text", "csv");
    public static final MimeType APPLICATION_JSONLINES = new MimeType("application", "jsonlines");
    public static final MimeType APPLICATION_JSONLINES_TEXT = new MimeType("application", "jsonlines",
        ImmutableMap.<String, String>builder().put("data", "text").build());
}
