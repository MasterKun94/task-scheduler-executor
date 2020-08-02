package com.oceanum.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenmingkun
 * @date 2020/8/1
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Injection(InjectType.REPOSITORY_FACTORY)
public @interface IRepositoryFactory {
    int priority() default 1;
}
