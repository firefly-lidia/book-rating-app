package com.lp.book.rating.app.limiter;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AuthApiRateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final BucketConfiguration authApiBucket;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public AuthApiRateLimitFilter(ProxyManager<byte[]> proxyManager, BucketConfiguration authApiBucket,
                                  HandlerExceptionResolver handlerExceptionResolver) {
        this.proxyManager = proxyManager;
        this.authApiBucket = authApiBucket;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !req.getServletPath().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain) throws ServletException, IOException {
        var currentIpBucketKey = "rate-limit:auth:%s".formatted(req.getRemoteAddr());

        var bucket = proxyManager.builder().build(currentIpBucketKey.getBytes(StandardCharsets.UTF_8), () -> authApiBucket);

        var probe = bucket.tryConsumeAndReturnRemaining(1);
        log.info("Checked Auth API IP rate limit for {} bucket. Result: {}", currentIpBucketKey, probe.isConsumed());

        if (!probe.isConsumed()) {
            var problemDetail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
            problemDetail.setTitle("Too Many Requests");
            problemDetail.setDetail("Rate limit exceeded. Please retry later.");
            problemDetail.setProperty("ip", req.getRemoteAddr());
            problemDetail.setProperty("retryAfter", TimeUnit.SECONDS.convert(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS));

            handlerExceptionResolver.resolveException(req, res, null, new ErrorResponseException(HttpStatus.TOO_MANY_REQUESTS, problemDetail, null));

            return;
        }

        chain.doFilter(req, res);
    }

}
