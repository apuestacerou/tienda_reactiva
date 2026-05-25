package com.tiendaenlinea.reactiva.scheduler;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.infrastructure.persistence.OrderR2dbcRepository;

import reactor.core.publisher.Mono;

@Component
public class ReportScheduler {

    private final OrderR2dbcRepository orderRepository;

    public ReportScheduler(OrderR2dbcRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void generarReporte() {

        Mono<Long> totalPedidos = orderRepository.count();

        Mono<Long> pedidosPendientes =
                orderRepository.countByStatus("PENDING");

        Mono<Long> pedidosCancelados =
                orderRepository.countByStatus("CANCELLED");

        Mono<BigDecimal> ventasTotales =
                orderRepository.totalVentas();

        Mono.zip(
                totalPedidos,
                pedidosPendientes,
                pedidosCancelados,
                ventasTotales
        ).subscribe(datos -> {

            System.out.println("\n===== REPORTE AUTOMATICO =====");

            System.out.println("Pedidos totales: " + datos.getT1());

            System.out.println("Pedidos pendientes: " + datos.getT2());

            System.out.println("Pedidos cancelados: " + datos.getT3());

            System.out.println("Ventas totales: $" + datos.getT4());

            System.out.println("==============================\n");
        });
    }
}