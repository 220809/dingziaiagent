package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.apiKey}")
    private String apiKey;

    @Test
    void executeWebSearch() {
        WebSearchTool tool = new WebSearchTool(apiKey);
        String query = "SpringAI 知识体系";
        String result = tool.executeWebSearch(query);
        Assertions.assertNotNull(result);
    }
}