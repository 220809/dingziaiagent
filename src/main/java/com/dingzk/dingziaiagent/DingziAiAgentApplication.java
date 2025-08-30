package com.dingzk.dingziaiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.dingzk.dingziaiagent.mapper"})
public class DingziAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DingziAiAgentApplication.class, args);
    }

}
