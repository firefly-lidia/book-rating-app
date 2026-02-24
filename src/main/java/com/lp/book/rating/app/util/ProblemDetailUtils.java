package com.lp.book.rating.app.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.OffsetDateTime;

@UtilityClass
public class ProblemDetailUtils {

    public static ProblemDetail build(HttpStatus status, String title, String detail, String slug) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }

    public static ProblemDetail enrich(ProblemDetail pd, HttpServletRequest request) {
        pd.setInstance(URI.create((request.getRequestURI())));
        pd.setProperty("timestamp", OffsetDateTime.now());
        var rid = request.getHeader("X-Request-Id");
        if (rid != null && !rid.isBlank()) {
            pd.setProperty("requestId", rid);
        }
        return pd;
    }

}
