package com.lp.book.rating.app.limiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.expiration.FixedTtlExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RedisClient redisClient(@Value("${spring.data.redis.url}") String url) {
        return RedisClient.create(url);
    }

    @Bean
    public StatefulRedisConnection<byte[], byte[]> connection(RedisClient client) {
        return client.connect(new ByteArrayCodec());
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(StatefulRedisConnection<byte[], byte[]> connection) {
        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(new FixedTtlExpirationAfterWriteStrategy(Duration.ofHours(1)))
                .build();
    }

    // here I added second limits for all bucket configs for "smoothing" max requests with per second limits, also initial tokens prevents to prevent "start burst blast"
    @Bean
    public BucketConfiguration authApiBucket() {
        var perMinute = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(5, Duration.ofMinutes(1))
                .initialTokens(3)
                .build();
        var per10s = Bandwidth.builder()
                .capacity(3)
                .refillGreedy(3, Duration.ofSeconds(10))
                .build();
        return BucketConfiguration.builder().addLimit(perMinute).addLimit(per10s).build();
    }

    //exactly 3 requests per/sec
    @Bean
    public BucketConfiguration jwtApiBucket() {
        var perMinute = Bandwidth.builder()
                .capacity(180)         // allow some burst, smooth after
                .refillGreedy(180, Duration.ofMinutes(1))
                .initialTokens(60)
                .build();
        var per10s = Bandwidth.builder()
                .capacity(30)
                .refillGreedy(30, Duration.ofSeconds(10))
                .build();
        return BucketConfiguration.builder().addLimit(perMinute).addLimit(per10s).build();
    }

    // Let`s try to calculate max burst at the beginning:
    // 15 tokens from the start due to per5s limit, perMinute limit recovers by 2 tokens/sec, per5s recovers by 3 tokens/sec => lost 1 token per s
    // we have 60 - 15 = 45 seconds in our reserve * 3 tokens per sec by perm5s = 135 tokens
    // we still have 15 seconds until the first min ends up, so here 15 * 2 (perMinute limit) = 30 tokens
    // Summary: at the beginning we could get 15 + 135 + 30 tokens = 180 tokens or requests during 1st minute, but than
    // if the load remains the same = max only 2 requests per second due to perMinute limit
//    @Bean
//    public BucketConfiguration globalBucket() {
//        var perMinute = Bandwidth.builder()
//                .capacity(120)
//                .refillGreedy(120, Duration.ofMinutes(1))
//                .initialTokens(60)
//                .build();
//        var per5s = Bandwidth.builder()
//                .capacity(15)
//                .refillGreedy(15, Duration.ofSeconds(5))
//                .build();
//        return BucketConfiguration.builder().addLimit(perMinute).addLimit(per5s).build();
//    }

}

