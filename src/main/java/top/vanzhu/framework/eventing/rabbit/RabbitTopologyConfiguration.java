package top.vanzhu.framework.eventing.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.stream.IntStream;

@Configuration
public class RabbitTopologyConfiguration implements InitializingBean {
    private @Autowired AmqpAdmin amqpAdmin;
    private @Autowired RabbitEventingSettings settings;

    @Override
    public void afterPropertiesSet() throws Exception {
        declareExchange();
        declareRoutableExchange();
        declareQueues();
        declareRoutableQueues();
        declareDeadLetterQueue();
    }

    private void declareRoutableQueues() {
        IntStream.range(0, settings.getRoutableQueues().size()).forEach(this::declareRoutableQueue);
    }

    private void declareExchange() {
        amqpAdmin.declareExchange(new TopicExchange(settings.getExchangeName(), true, false));
    }

    private void declareQueues() {
        IntStream.range(0, settings.getQueues().size()).forEach(this::declareQueue);
    }

    private void declareRoutableExchange() {
        amqpAdmin.declareExchange(new TopicExchange(settings.getRoutableExchangeName(), true, false));
    }

    private void declareDeadLetterQueue() {
        if (StringUtils.hasText(settings.getDeadLetterQueue())) {
            declareQueue(settings.getDeadLetterQueue(), settings.getDeadLetterRoutingKey(), settings.getRoutableExchangeName());
        }
    }

    private void declareQueue(String queueName, String routingKey, String exchangeName) {
        amqpAdmin.declareExchange(new TopicExchange(exchangeName, true, false));
        amqpAdmin.declareQueue(new Queue(queueName, true, false, false));
        amqpAdmin.declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null));
    }

    private void declareQueue(int index) {
        String queueName = settings.getQueues().get(index);
        String routingKey = settings.getRoutingKey(index);
        String exchangeName = settings.getExchangeName();
        declareQueue(queueName, routingKey, exchangeName);
    }

    private void declareRoutableQueue(int index) {
        String queueName = settings.getRoutableQueues().get(index);
        String routingKey = settings.getRoutableRoutingKey(index);
        String exchangeName = settings.getRoutableExchangeName();
        declareQueue(queueName, routingKey, exchangeName);
    }
}
