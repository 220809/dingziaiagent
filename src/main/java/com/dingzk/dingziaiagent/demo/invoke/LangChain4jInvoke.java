package com.dingzk.dingziaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

public class LangChain4jInvoke {
    public static void main(String[] args) {
        ChatModel qwenModel = QwenChatModel.builder()
                .apiKey(TestApiKey.ApiKey)
                .modelName("qwen-max")
                .build();

        String result = qwenModel.chat("你好，我是钉子，很高兴见到你！");
        System.out.println(result);
    }
}
