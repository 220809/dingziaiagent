package com.dingzk.dingziaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LiZhiAppTest {

    @Resource
    private LiZhiApp liZhiApp;
    @Test
    public void testDoChat() {
        String chatId = UUID.randomUUID().toString();

        // 第一条消息
        String message = "你好，我是钉子。";
        liZhiApp.doChat(message, chatId);
        // 第二条消息
        message = "你好，我是钉子。最近被家人催婚，我该怎么办？";
        liZhiApp.doChat(message, chatId);
        // 第三条消息
//        message = "请问我是谁？刚才和你说过。";
//        liZhiApp.doChat(message, chatId);
    }

    @Test
    void doStructuredChat() {
        String chatId = UUID.randomUUID().toString();

        // 第一条消息
        String message = "你好，我是钉子。最近被家人催婚，我该怎么办？请给我一些排解烦恼的建议。";
        LiZhiApp.StructuredReport report = liZhiApp.doStructuredChat(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void testSensitiveWordsAdvisor() {
        String chatId = UUID.randomUUID().toString();
        // 违禁词
        String message = "你好，我是恐怖";
        liZhiApp.doChat(message, chatId);

        // 非违禁词
        message = "你好，我是恐布";
        liZhiApp.doChat(message, chatId);
    }
}