package com.bsoft.deploy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bsoft.deploy")
@MapperScan("com.bsoft.deploy.dao.mapper")
public class SlaveApplication {
    private static ApplicationContext appContext;

    public static void main(String[] args) {
        appContext = SpringApplication.run(SlaveApplication.class, args);
    }

    public static ApplicationContext getContext() {
        return appContext;
    }
}
