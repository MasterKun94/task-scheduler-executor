package com.oceanum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Injection(InjectType.STD_HANDLER_FACTORY)
public @interface IStdHandlerFactory {
    int priority() default 1;
}
