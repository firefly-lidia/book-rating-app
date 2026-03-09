package com.lp.book.rating.app.util;

import com.lp.book.rating.app.exception.InvalidETagFormatException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ETagUtils {

    public static String buildETag(int version) {
        return "\"v" + version + "\"";
    }

    public static  Integer extractETag(String etag) {
        if (etag.isBlank() || !etag.startsWith("\"v") || !etag.endsWith("\"")) {
            throw new InvalidETagFormatException("Invalid ETag format");
        }

        try {
            return Integer.parseInt(etag.substring(1, etag.length() - 1));
        } catch (NumberFormatException e) {
            throw new InvalidETagFormatException("Invalid ETag format");
        }
    }

}
