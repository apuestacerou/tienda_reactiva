package com.tiendaenlinea.reactiva.rabbit;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.Receiver;

// @Service
// @RequiredArgsConstructor
public class OrderConsumer {

    private Receiver receiver;

    @PostConstruct
    public void consumir() {

        receiver.consumeAutoAck(RabbitConfig.QUEUE)
                .map(delivery -> new String(delivery.getBody()))
                .subscribe(mensaje -> {
                    System.out.println("Mensaje recibido: " + mensaje);
                });
    }
}