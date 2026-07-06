package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解,用于表示某些方法进行自动填充处理
 */
@Target(ElementType.METHOD)//说明注解只能放的位置,只能放到方法上
@Retention(RetentionPolicy.RUNTIME)
public  @interface AutoFill {
    //数据库操作类型 insert ,update
    OperationType value();
}
