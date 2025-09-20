package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfGeneratorToolTest {

    @Test
    void executePdfGeneration() {
        PdfGeneratorTool tool = new PdfGeneratorTool();
        String content = "Spring 是一个开源的 Java 企业级应用开发框架，由 Rod Johnson 于 2003 年创建。" +
                "它提供了一套全面的编程和配置模型，用于构建现代化的 Java 应用程序。";
        String fileName = "spring.pdf";
        String result = tool.executePdfGeneration(content, fileName);
        Assertions.assertNotNull(result);
    }
}