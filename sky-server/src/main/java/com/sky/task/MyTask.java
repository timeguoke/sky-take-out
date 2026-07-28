package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;


//学习Spring Task使用
@Component
@Slf4j
public class MyTask {

    //@Scheduled(cron = "0/10 * * * * ? ")
    public void task01(){
        log.info("王佳俊最帅"+ new Date());

    }
}
