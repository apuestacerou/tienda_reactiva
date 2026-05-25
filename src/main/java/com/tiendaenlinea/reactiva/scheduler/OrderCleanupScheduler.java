package com.tiendaenlinea.reactiva.scheduler;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderR2dbcRepository;

@Component
public class OrderCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderCleanupScheduler.class);

    private final OrderR2dbcRepository orderRepository;

    public OrderCleanupScheduler(OrderR2dbcRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void cancelarPedidosPendientes() {

        log.info("scheduler ejecutandose..... ");

        Instant limite = Instant.now().minus(30, ChronoUnit.MINUTES);

        orderRepository.findPendingOrdersOlderThan(limite)
                .flatMap(order -> {
                    order.setStatus("CANCELLED");

                    log.info("Pedido cancelado automaticamente: {}", order.getId());

                    return orderRepository.save(order);
                })
                .subscribe();
    }
}
// Cada minuto: --> 0 */1 * * * *
//Spring:
//busca pedidos pendientes,
//revisa si tienen más de 30 minutos,
//los cancela automáticamente.