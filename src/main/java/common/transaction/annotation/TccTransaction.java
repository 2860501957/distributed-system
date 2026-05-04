package com.wuhan.seckill.common.transaction.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TccTransaction {
    String tryMethod() default "";
    String confirmMethod() default "";
    String cancelMethod() default "";
}