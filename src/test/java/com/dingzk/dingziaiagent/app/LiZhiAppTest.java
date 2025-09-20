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

    @Test
    void doRagChat() {
        String chatId = UUID.randomUUID().toString();
        String message = "我总是后悔当初没能好好学习，我该如何排解这种情绪？";

        String content = liZhiApp.doRagChat(message, chatId);
        Assertions.assertNotNull(content);
    }

    @Test
    void doChatWithTools() {
//        // 测试网络搜索工具调用
        testMessage("最近想要散散心，想去北京旅游，请帮我搜索一篇北京旅游攻略");
//
//        // 测试网络抓取工具调用
        testMessage("请帮我总结一下这篇北京旅游攻略：https://zhuanlan.zhihu.com/p/638327554");
//
//        // 测试文件下载工具调用
        testMessage("帮我下载一张北京天安门的图片");

        // 测试终端操作工具调用
        testMessage("使用 'java' 指令执行此 java 代码文件：D:\\Code\\Java\\projects\\dingzi-ai-agent\\tmp\\file\\Helloworld.java");
//
//        // 测试文件操作工具调用
        testMessage("保存我的旅游规划为文件");
//
//        // 测试Pdf生成工具调用
        testMessage("生成一份‘北京旅游攻略’的pdf文件，包括出行策略、游玩顺序等内容");
    }

    private void testMessage(String message) {
        String conversationId = UUID.randomUUID().toString();
        String result = liZhiApp.doChatWithTools(message, conversationId);
        Assertions.assertNotNull(result);
    }
}