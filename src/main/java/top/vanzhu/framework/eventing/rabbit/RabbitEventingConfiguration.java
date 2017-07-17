package top.vanzhu.framework.eventing.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.config.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.MapRetryContextCache;

@Configuration
@EnableConfigurationProperties(RabbitEventingSettings.class)
public class RabbitEventingConfiguration {
    private @Autowired RabbitEventingSettings settings;
    private @Autowired ConnectionFactory connectionFactory;
    private @Autowired RabbitTemplate rabbitTemplate;
    private @Autowired ObjectMapper objectMapper;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setChannelTransacted(true);
        factory.setAdviceChain(createMissingMessageIdAdvice(), createListenerRetryInterceptor());
        return factory;
    }

    private MissingMessageIdAdvice createMissingMessageIdAdvice() {
        return new MissingMessageIdAdvice(new MapRetryContextCache());
    }

    private StatefulRetryOperationsInterceptor createListenerRetryInterceptor() {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(5)
                .backOffOptions(1000, 2, 100000)
                .recoverer(createMessageRecoverer())
                .build();
    }

    private MessageRecoverer createMessageRecoverer() {
        return new RepublishMessageRecoverer(
                rabbitTemplate, settings.getRoutableExchangeName(), settings.getDeadLetterRoutingKey()
        );
    }

    @Bean
    public RabbitEventForwarder rabbitEventForwarder() {
        return new RabbitEventForwarder(rabbitTemplate, settings.getExchangeName(), settings.getRoutableExchangeName(), objectMapper);
    }

    @Bean
    public ExternalEventListenerBeanPostProcessor externalEventListenerBeanPostProcessor() {
        return new ExternalEventListenerBeanPostProcessor();
    }
}
