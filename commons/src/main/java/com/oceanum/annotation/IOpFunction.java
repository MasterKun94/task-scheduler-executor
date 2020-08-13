package com.oceanum.annotation;

import com.googlecode.aviator.lexer.token.OperatorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenmingkun
 * @date 2020/7/31
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Injection(InjectType.OPERATOR_FUNCTION)
public @interface IOpFunction { }
