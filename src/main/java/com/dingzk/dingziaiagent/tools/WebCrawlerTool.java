package com.dingzk.dingziaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebCrawlerTool {

    @Tool(description = "This is a tool which can be used to crawl the content of a web page")
    public String executeWebCrawl(@ToolParam(description = "The url of the crawled web page") String url) {
        try {
            // 连接网页并获取Document对象
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000) // 10秒超时
                    .get();

            return document.html();

        } catch (IOException e) {
            return "Error occurred when crawling from the web page: " + e.getMessage();
        }
    }
}
