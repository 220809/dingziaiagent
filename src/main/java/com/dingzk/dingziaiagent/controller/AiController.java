package com.dingzk.dingziaiagent.controller;

import com.dingzk.dingziaiagent.agent.JManus;
import com.dingzk.dingziaiagent.app.LiZhiApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LiZhiApp liZhiApp;

    @Resource
    private List<ToolCallback> registeredTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping(value = "/emo/consultant", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> emoConsultant(String message, String conversationId) {
        return liZhiApp.doRagChatStream(message, conversationId);
    }

    @GetMapping("/jmanus/chat")
    public SseEmitter jManusChat(String message) {
        JManus jManus = new JManus(registeredTools, dashscopeChatModel);
        return jManus.run(message);
    }
}
