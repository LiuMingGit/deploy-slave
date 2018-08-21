package com.bsoft.deploy.bean;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.context.store.SlaveStore;
import com.bsoft.deploy.netty.client.SimpleFileClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc
 * Created on 2018/8/15.
 *
 * @author yangl
 */
@Configuration
public class BeanFactory {

    @Bean(initMethod = "start")
    SimpleFileClient fileServer() {
        return new SimpleFileClient();
    }

    @Bean
    Global initGlobal() {
        return new Global();
    }

    @Bean
    SlaveStore createSlaveStore() {
        return new SlaveStore();
    }
}
