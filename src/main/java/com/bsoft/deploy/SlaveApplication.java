package com.bsoft.deploy;

import com.bsoft.deploy.context.Global;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.bsoft.deploy")
@MapperScan("com.bsoft.deploy.dao.mapper")
public class SlaveApplication {


    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(SlaveApplication.class, args);
        Global.setAppContext(appContext);
    }


}
