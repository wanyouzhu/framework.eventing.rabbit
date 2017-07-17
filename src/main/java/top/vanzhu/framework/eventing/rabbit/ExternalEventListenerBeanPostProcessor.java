package top.vanzhu.framework.eventing.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.config.*;
import org.springframework.amqp.rabbit.listener.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class ExternalEventListenerBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware, SmartInitializingSingleton {
    private final RabbitListenerEndpointRegistrar registrar = new RabbitListenerEndpointRegistrar();
    private @Autowired RabbitEventingSettings settings;
    private @Autowired RabbitListenerContainerFactory containerFactory;
    private ExternalEventDispatcher externalEventDispatcher;
    private BeanFactory beanFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        processAnnotation(bean);
        return bean;
    }

    private void processAnnotation(Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        ReflectionUtils.doWithMethods(targetClass, x -> processAnnotatedBeanMethod(x, bean), ReflectionUtils.USER_DECLARED_METHODS);
    }

    private void processAnnotatedBeanMethod(Method method, Object bean) {
        ExternalEventHandler eventHandler = AnnotationUtils.findAnnotation(method, ExternalEventHandler.class);
        if (eventHandler != null) {
            processHandler(eventHandler, method, bean);
        }
    }

    private void processHandler(ExternalEventHandler eventHandler, Method method, Object bean) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        checkAnnotatedMethod(parameterTypes);
        externalEventDispatcher.registerHandler(new MethodEventHandler(eventHandler.event(), bean, method, parameterTypes[0]));
    }

    private void checkAnnotatedMethod(Class<?>[] parameterTypes) {
        checkParameterCount(parameterTypes);
    }

    private void checkParameterCount(Class<?>[] parameterTypes) {
        if (parameterTypes.length != 1) {
            throw new IllegalStateException("The annotated event-handler should contain only one parameter.");
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] queues = settings.overallQueueNames();
        if (queues.length == 0) return;
        initializeRegistrar();
        registerEndpoint(queues);
        this.registrar.afterPropertiesSet();
    }

    private void initializeRegistrar() {
        this.registrar.setBeanFactory(this.beanFactory);
        this.registrar.setEndpointRegistry(getEndpointRegistry());
    }

    private void registerEndpoint(String[] queues) {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setMessageListener(externalEventDispatcher);
        endpoint.setBeanFactory(this.beanFactory);
        endpoint.setId("default-message-listener");
        endpoint.setQueueNames(queues);
        this.registrar.registerEndpoint(endpoint, containerFactory);
    }

    private RabbitListenerEndpointRegistry getEndpointRegistry() {
        return this.beanFactory.getBean(
                RabbitListenerConfigUtils.RABBIT_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
                RabbitListenerEndpointRegistry.class
        );
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        this.externalEventDispatcher = new ExternalEventDispatcher(beanFactory.getBean(ObjectMapper.class));
    }
}
