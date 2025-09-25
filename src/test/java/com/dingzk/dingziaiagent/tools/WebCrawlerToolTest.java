package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebCrawlerToolTest {

    @Test
    void executeWebCrawl() {
        WebCrawlerTool tool = new WebCrawlerTool();
        String url = "https://www.visitbeijing.com.cn/article/4OFBGTXh6jd";
        String result = tool.executeWebCrawl(url);

        Assertions.assertNotNull(result);
    }
}