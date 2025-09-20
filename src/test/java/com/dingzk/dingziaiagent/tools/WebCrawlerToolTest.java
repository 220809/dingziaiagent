package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebCrawlerToolTest {

    @Test
    void executeWebCrawl() {
        WebCrawlerTool tool = new WebCrawlerTool();
        String url = "https://docs.spring.io/spring-ai/reference/getting-started.html";
        String result = tool.executeWebCrawl(url);

        Assertions.assertNotNull(result);
    }
}