package com.dingzk.imagegenmcpserver;

import com.dingzk.imagegenmcpserver.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImageGenMcpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImageGenMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider imageMcpTool(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }
}
