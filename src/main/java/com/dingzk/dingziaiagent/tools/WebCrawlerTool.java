package com.dingzk.dingziaiagent.tools;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

            // 2. 提取正文文字内容
            return extractContent(document);

        } catch (IOException e) {
            return "Error occurred when crawling from the web page: " + e.getMessage();
        }
    }

    /**
     * 提取正文文字内容
     */
    private String extractContent(Document document) {
        StringBuilder content = new StringBuilder();

        // 策略2：提取段落文本
        Elements paragraphElements = document.select("p");
        if (!paragraphElements.isEmpty()) {
            for (Element p : paragraphElements) {
                String text = p.text().trim();
                if (text.length() > 10) { // 过滤过短的段落
                    content.append(text).append("\n");
                }
            }
            // 限制返回内容大小
            return StringUtils.substring(content.toString().trim(), 0, 10000);
        }

        return "未能提取到有效的正文内容";
    }
}
