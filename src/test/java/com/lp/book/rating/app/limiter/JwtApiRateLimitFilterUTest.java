package com.lp.book.rating.app.limiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtApiRateLimitFilterUTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private RemoteBucketBuilder<byte[]> remoteBucketBuilder;

    @Mock
    private ProxyManager<byte[]> proxyManager;

    @Mock
    private BucketProxy bucketProxy;

    @Mock
    private ConsumptionProbe probe;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private JwtAuthenticationToken authentication;

    @Mock
    private Jwt jwt;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    @Captor
    private ArgumentCaptor<ErrorResponseException> errorCaptor;

    @InjectMocks
    private JwtApiRateLimitFilter filter;

    private final BucketConfiguration authApiBucket = BucketConfiguration
            .builder()
            .addLimit(Bandwidth.builder()
                    .capacity(1)
                    .refillIntervally(1, Duration.ofSeconds(1))
                    .build())
            .build();

    @Test
    void shouldNotFilter() {
        when(request.getServletPath()).thenReturn("/public/health");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldFilterJwtApi() {
        when(request.getServletPath()).thenReturn("/api/user");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterInternal_success() throws ServletException, IOException {
        mockBucket();
        mockJwt();

        when(probe.isConsumed()).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver, never()).resolveException(any(), any(), any(), any());

        verify(proxyManager.builder()).build(
                argThat(bytes -> new String(bytes, StandardCharsets.UTF_8)
                        .equals("rate-limit:user:1")),
                any(Supplier.class)
        );
    }

    @Test
    void doFilterInternal_too_many_requests() throws ServletException, IOException {
        mockJwt();
        mockBucket();

        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(TimeUnit.SECONDS.toNanos(3));

        filter.doFilterInternal(request, response, filterChain);

        verify(handlerExceptionResolver).resolveException(eq(request), eq(response), isNull(), errorCaptor.capture());

        var ex = errorCaptor.getValue();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        var problemDetail = ex.getBody();

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Too Many Requests");
        assertThat(problemDetail.getDetail()).isEqualTo("Rate limit exceeded. Please retry later.");
        assertThat(problemDetail.getProperties()).containsEntry("retryAfter", 3L);
    }

    private void mockBucket() {
        when(proxyManager.builder()).thenReturn(remoteBucketBuilder);
        when(remoteBucketBuilder.build(any(byte[].class), any(Supplier.class))).thenReturn(bucketProxy);
        when(bucketProxy.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
    }

    private void mockJwt() {
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getToken()).thenReturn(jwt);

        when(jwt.getClaims()).thenReturn(Map.of("uid", "1"));

        SecurityContextHolder.setContext(securityContext);
    }
}