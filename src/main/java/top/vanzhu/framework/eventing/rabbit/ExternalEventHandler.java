package top.vanzhu.framework.eventing.rabbit;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalEventHandler {
    @AliasFor("event")
    String value() default "";

    @AliasFor("value")
    String event() default "";
}
