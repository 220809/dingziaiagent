package com.dingzk.dingziaiagent.tools;

import cn.hutool.core.io.FileUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;

class PdfGeneratorToolTest {

    @Test
    void executePdfGeneration() {
        PdfGeneratorTool tool = new PdfGeneratorTool();
        String content =
                FileUtil.readString(new File("tiananmen_guide.md")  // 改为可访问到的 md 文档
                , StandardCharsets.UTF_8);
        String fileName = "spring.pdf";
        String result = tool.executePdfGeneration(content, fileName);
        Assertions.assertNotNull(result);
    }
}