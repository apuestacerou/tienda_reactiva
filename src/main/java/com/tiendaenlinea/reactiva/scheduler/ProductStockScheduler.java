package com.tiendaenlinea.reactiva.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tiendaenlinea.reactiva.infrastructure.persistence.ProductR2dbcRepository;

@Component
public class ProductStockScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductStockScheduler.class);

    private final ProductR2dbcRepository productRepository;

    public ProductStockScheduler(ProductR2dbcRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void monitorearProductosAgotados() {

        log.info("Revisando productos agotados...");

        productRepository.findOutOfStockProducts()
                .doOnNext(product -> log.warn(
                        "Producto agotado detectado: {} | Stock: {}",
                        product.getName(),
                        product.getStock()))
                .subscribe();
    }
}

//
//  Cada minuto:
//revisa productos con stock 0
// imprime alerta
// usa scheduler
// usa programación reactiva
// usa Flux
// usa R2DBC
// usa cron
// usa logs   