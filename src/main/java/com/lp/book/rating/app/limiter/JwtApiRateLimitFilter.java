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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtApiRateLimitFilter extends OncePerRequestFilter {

    private final ProxyManager<byte[]> proxyManager;
    private final BucketConfiguration jwtApiBucket;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtApiRateLimitFilter(ProxyManager<byte[]> proxyManager, BucketConfiguration jwtApiBucket,
                                 HandlerExceptionResolver handlerExceptionResolver) {
        this.proxyManager = proxyManager;
        this.jwtApiBucket = jwtApiBucket;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        var path = req.getServletPath();
        return !path.startsWith("/api/") || path.startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest req, @NonNull HttpServletResponse res, @NonNull FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken token && token.isAuthenticated()) {
            var jwt = token.getToken();
            var userId = jwt.getClaims().get("uid").toString();

            var currentUserBucketKey = "rate-limit:user:%s".formatted(userId);
            var bucket = proxyManager.builder().build(currentUserBucketKey.getBytes(StandardCharsets.UTF_8), () -> jwtApiBucket);

            var probe = bucket.tryConsumeAndReturnRemaining(1);
            log.info("Checked JWT APIs rate limit for {} bucket. Result: {}", currentUserBucketKey, probe.isConsumed());

            if (!probe.isConsumed()) {
                var problemDetail = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
                problemDetail.setTitle("Too Many Requests");
                problemDetail.setDetail("Rate limit exceeded. Please retry later.");
                problemDetail.setProperty("retryAfter", TimeUnit.SECONDS.convert(probe.getNanosToWaitForRefill(), TimeUnit.NANOSECONDS));

                handlerExceptionResolver.resolveException(req, res, null, new ErrorResponseException(HttpStatus.TOO_MANY_REQUESTS, problemDetail, null));
                return;
            }

            chain.doFilter(req, res);

            return;
        }

        chain.doFilter(req, res);
    }
}
