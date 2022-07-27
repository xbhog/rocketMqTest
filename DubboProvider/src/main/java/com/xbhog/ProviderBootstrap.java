package com.xbhog;

import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author xbhog
 * @describe:
 * @date 2022/7/26
 */
@EnableDubboConfiguration
@SpringBootApplication
public class ProviderBootstrap {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(ProviderBootstrap.class,args);
    }
}
