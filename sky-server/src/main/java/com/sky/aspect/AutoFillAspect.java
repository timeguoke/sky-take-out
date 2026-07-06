package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面,实现公共区域自动填充
 */
@Aspect//切面注解
@Component//加载成Bean
@Slf4j//日志
public class AutoFillAspect {
    /**
     * 切入点
     */
    //拦截&&前复合条件的方法并且带有注解是后面的
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }


    /**
     * 通知
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint jp) {
        log.info("开始公共字段自动填充...");
        //获取被当前方法拦截的数据库操作方法类型
        //现获取方法签名对象
        MethodSignature signature = (MethodSignature) jp.getSignature();
        //获取该方法上面的注解对象AutoFill
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        //获取操作类型(是insert,还是update)
        OperationType operationType = autoFill.value();
        //获取方法的参数
        Object[] args = jp.getArgs();
        //没有参数的情况
        if (args == null && args.length == 0) {
            return;
        }
        //有参数的情况
        Object entity = args[0];


        //准备赋值的对象
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //根据不同的操作类型进行不同的分类
        if (operationType == OperationType.INSERT) {
            //给四个公共字段进行赋值
            try {
                //获取set方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //给set方法进行赋值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            //给来两个字段进行赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }

}
