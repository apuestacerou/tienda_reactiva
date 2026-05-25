package com.tiendaenlinea.reactiva.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisTokenMonitorScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(RedisTokenMonitorScheduler.class);

    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisTokenMonitorScheduler(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void monitorRedisTokens() {

        log.info("Iniciando monitoreo de tokens Redis...");

        redisTemplate.keys("refresh:*")
                .collectList()
                .doOnNext(tokens -> {

                    log.info("Cantidad de refresh tokens activos: {}", tokens.size());

                    tokens.forEach(token ->
                            log.info("Token encontrado: {}", token)
                    );
                })
                .subscribe();
    }
}