package top.vanzhu.framework.eventing.rabbit;

import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodEventHandler {
    private final String eventName;
    private final Object bean;
    private final Method method;
    private final Class<?> payloadType;

    public MethodEventHandler(String eventName, Object bean, Method method, Class<?> payloadType) {
        this.eventName = eventName;
        this.bean = bean;
        this.method = method;
        this.payloadType = payloadType;
        check();
    }

    private void check() {
        Assert.hasText(eventName, "Event name can not be blank.");
        Assert.notNull(bean, "The host object of a method handler can not be null.");
        Assert.notNull(method, "The method can not be null.");
        Assert.notNull(payloadType, "The java-type of event payload can not be null.");
    }

    public String getEventName() {
        return eventName;
    }

    public Class<?> getPayloadType() {
        return payloadType;
    }

    public void handle(Object event) {
        try {
            method.invoke(bean, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
