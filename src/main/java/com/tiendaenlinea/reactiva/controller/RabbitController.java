package com.tiendaenlinea.reactiva.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tiendaenlinea.reactiva.rabbit.OrderProducer;

@RestController
public class RabbitController {

    private final OrderProducer producer;

    public RabbitController(OrderProducer producer) {
        this.producer = producer;
    }

    @GetMapping("/enviar")
    public String enviarMensaje() {

        producer.send("Pedido realizado correctamente");

        return "Mensaje enviado";
    }
}