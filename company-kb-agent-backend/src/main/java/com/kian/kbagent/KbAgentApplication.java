package com.kian.kbagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.kian.kbagent.mapper")
@ConfigurationPropertiesScan
public class KbAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KbAgentApplication.class, args);
    }
}
