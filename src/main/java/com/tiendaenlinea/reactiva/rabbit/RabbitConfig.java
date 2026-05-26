package com.tiendaenlinea.reactiva.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.*;

@Slf4j
// @Configuration
public class RabbitConfig {

    public static final String QUEUE = "orders.pending";

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("localhost");
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setPort(5672);

        return factory;
    }

    @Bean
    public Sender sender(ConnectionFactory connectionFactory) {
        return RabbitFlux.createSender(
                new SenderOptions().connectionFactory(connectionFactory)
        );
    }

    @Bean
    public Receiver receiver(ConnectionFactory connectionFactory) {
        return RabbitFlux.createReceiver(
                new ReceiverOptions().connectionFactory(connectionFactory)
        );
    }

    @Bean
    public CommandLineRunner declareQueue(Sender sender) {
        return args -> {
            log.info("Creando cola: {}", QUEUE);

            sender.declareQueue(
                    QueueSpecification.queue(QUEUE)
            ).block();

            log.info("Cola creada correctamente");
        };
    }
}