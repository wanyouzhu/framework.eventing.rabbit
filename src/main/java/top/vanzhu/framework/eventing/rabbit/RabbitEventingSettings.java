package top.vanzhu.framework.eventing.rabbit;

import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

@ConfigurationProperties("application.eventing.rabbitmq")
public class RabbitEventingSettings {
    public static final String QUEUE_NAMES_PROPERTY = "application.eventing.rabbitmq.queues";
    private @NotEmpty String exchangeName;
    private @NotEmpty String routableExchangeName;
    private List<String> queues = Lists.newArrayList();
    private List<String> routingKeys = Lists.newArrayList();
    private List<String> routableQueues = Lists.newArrayList();
    private List<String> routableRoutingKeys = Lists.newArrayList();
    private String deadLetterQueue;
    private String deadLetterRoutingKey;

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutableExchangeName() {
        return routableExchangeName;
    }

    public void setRoutableExchangeName(String routableExchangeName) {
        this.routableExchangeName = routableExchangeName;
    }

    public List<String> getQueues() {
        return queues;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }

    public List<String> getRoutableQueues() {
        return routableQueues;
    }

    public void setRoutableQueues(List<String> routableQueues) {
        this.routableQueues = routableQueues;
    }

    public String[] overallQueueNames() {
        List<String> result = Lists.newArrayList();
        result.addAll(getQueues());
        result.addAll(getRoutableQueues());
        return result.toArray(new String[0]);
    }

    public List<String> getRoutableRoutingKeys() {
        return routableRoutingKeys;
    }

    public void setRoutableRoutingKeys(List<String> routableRoutingKeys) {
        this.routableRoutingKeys = routableRoutingKeys;
    }

    public String getRoutableRoutingKey(int index) {
        if (index < 0 || index >= routableRoutingKeys.size()) return "#";
        String routingKey = routableRoutingKeys.get(index);
        return isBlank(routingKey) ? "#" : routingKey;
    }

    public String getDeadLetterQueue() {
        return deadLetterQueue;
    }

    public void setDeadLetterQueue(String deadLetterQueue) {
        this.deadLetterQueue = deadLetterQueue;
    }

    public String getDeadLetterRoutingKey() {
        Assert.hasText(getDeadLetterQueue(), "Missing property 'application.eventing.rabbitmq.dead-letter-queue'.");
        Assert.hasText(deadLetterRoutingKey, "Missing property 'application.eventing.rabbitmq.dead-letter-routing-key'.");
        return deadLetterRoutingKey;
    }

    public void setDeadLetterRoutingKey(String deadLetterRoutingKey) {
        this.deadLetterRoutingKey = deadLetterRoutingKey;
    }

    public List<String> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(List<String> routingKeys) {
        this.routingKeys = routingKeys;
    }

    public String getRoutingKey(int index) {
        if (index < 0 || index >= routingKeys.size()) return "#";
        String routingKey = routingKeys.get(index);
        return isBlank(routingKey) ? "#" : routingKey;
    }
}
