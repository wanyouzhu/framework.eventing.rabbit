package top.vanzhu.framework.eventing.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import top.vanzhu.framework.eventing.*;

public class RabbitEventForwarder implements EventForwarder {
    private final AmqpTemplate amqpTemplate;
    private final String exchangeName;
    private final String routableExchangeName;
    private final ObjectMapper objectMapper;

    public RabbitEventForwarder(AmqpTemplate amqpTemplate, String exchangeName, String routableExchangeName, ObjectMapper objectMapper) {
        this.amqpTemplate = amqpTemplate;
        this.exchangeName = exchangeName;
        this.routableExchangeName = routableExchangeName;
        this.objectMapper = objectMapper;
    }

    @Override
    public void forward(DomainEvent event) {
        amqpTemplate.send(exchangeName, event.eventName(), createMessage(event));
    }

    @Override
    public void forward(DomainEvent event, String routingHint) {
        amqpTemplate.send(routableExchangeName, routingHint, createMessage(event));
    }

    private Message createMessage(DomainEvent event) {
        return new Message(createMessageBody(event), createMessageProperties(event));
    }

    private byte[] createMessageBody(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event.eventPayload()).getBytes(Charsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("serialization error", e);
        }
    }

    private MessageProperties createMessageProperties(DomainEvent event) {
        MessageProperties properties = new MessageProperties();
        properties.setContentEncoding(Charsets.UTF_8.name());
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setMessageId(event.eventId());
        properties.setHeader("event-name", event.eventName());
        properties.setHeader("event-type", event.eventName());
        return properties;
    }
}
