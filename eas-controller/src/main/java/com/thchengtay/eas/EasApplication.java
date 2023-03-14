package com.thchengtay.eas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * thctay-eas 启动类
 */
@MapperScan(value = {"com.thchengtay.eas.dao"})
@ComponentScan(value = {"com.thchengtay"})
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@EnableAsync
public class EasApplication
{
    public static void main(String[] args) {
        SpringApplication.run(EasApplication.class, args);
    }

}
