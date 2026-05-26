package com.tiendaenlinea.reactiva.rabbit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

// @Service
// @RequiredArgsConstructor
public class OrderProducer {

    private Sender sender;

    public void send(String mensaje) {

        sender.send(
                reactor.core.publisher.Mono.just(
                        new OutboundMessage(
                                "",
                                RabbitConfig.QUEUE,
                                mensaje.getBytes()
                        )
                )
        ).subscribe();

        System.out.println("Mensaje enviado: " + mensaje);
    }
}