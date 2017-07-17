package top.vanzhu.framework.eventing.rabbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

public class ExternalEventDispatcher implements MessageListener {
    private final Map<String, MethodEventHandler> handlers = Maps.newHashMap();
    private final Logger logger = Logger.getLogger(ExternalEventDispatcher.class);
    private final ObjectMapper objectMapper;

    public ExternalEventDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void registerHandler(MethodEventHandler handler) {
        checkDuplicateHandler(handler);
        handlers.put(handler.getEventName(), handler);
    }

    private void checkDuplicateHandler(MethodEventHandler handler) {
        if (this.handlers.containsKey(handler.getEventName())) {
            throw new IllegalStateException("The event handler for event '" + handler.getEventName() + "' has been registered.");
        }
    }

    @Override
    public void onMessage(Message message) {
        String eventName = (String) (message.getMessageProperties().getHeaders().get("event-name"));
        if (!StringUtils.hasText(eventName)) {
            logger.warn("Ignored malformed message: " + message);
            return;
        }

        MethodEventHandler handler = getEventHandler(eventName);
        if (handler == null) return;
        handler.handle(convertMessage(message, handler));
    }

    private MethodEventHandler getEventHandler(String eventName) {
        return handlers.get(eventName);
    }

    private Object convertMessage(Message message, MethodEventHandler handler) {
        try {
            return objectMapper.readValue(message.getBody(), handler.getPayloadType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert message.", e);
        }
    }
}
