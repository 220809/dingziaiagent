package com.dingzk.dingziaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dingzk.dingziaiagent.agent.state.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Data
public abstract class BaseAgent {

    /**
     * 基础属性
     */
    private String name;
    private String description;

    private String systemPrompt;
    private String nextStepPrompt;

    // 消息
    private List<Message> messageList = new ArrayList<>();

    // 执行步骤
    private int currentStep = 0;
    private int maxAttemptStep = 10;

    // 智能体状态
    private AgentState currentState = AgentState.IDLE;

    // 大模型
    private ChatClient chatClient;

    private int duplicateExecutionThreshold = 2;

    protected SseEmitter sseEmitter;

    /**
     * 智能体执行循环
     */
    public SseEmitter run(String userPrompt) {
        sseEmitter = new SseEmitter(300000L);  // 5 分钟超时时间

        CompletableFuture.runAsync(() -> {
            try {
                if (currentState != AgentState.IDLE) {
                    sseEmitter.send(String.format("此状态{ %s }下无法启动智能体: ", currentState.description));
                    sseEmitter.complete();
                    return;
                }

                if (StrUtil.isBlankIfStr(userPrompt)) {
                    sseEmitter.send("无用户输入");
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                log.error("SSE output error: ", e);
                sseEmitter.completeWithError(e);
            }

            currentState = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));

            List<String> results = new ArrayList<>();
            try {
                while (currentStep < maxAttemptStep && currentState != AgentState.FINISHED) {
                    ++currentStep;
                    log.info("执行步骤: {}/{}", currentStep, maxAttemptStep);
                    String stepResult = String.format("步骤 %d: %s", currentStep, step());

//                    sseEmitter.send(stepResult);
                    if (currentState == AgentState.FINISHED) {
                        sseEmitter.complete();
                    }

                    if (isStuck()) {
                        handleStuck();
                    }
                    results.add(stepResult);
                }
                if (currentStep >= maxAttemptStep) {
                    currentState = AgentState.IDLE;
                    currentStep = 0;
                    sseEmitter.send(String.format("执行结束: 执行步骤超出最大值: %d", maxAttemptStep));
                    sseEmitter.complete();
                    results.add(String.format("执行结束: 执行步骤超出最大值: %d", maxAttemptStep));
                }
            } catch (Exception e) {
                log.error("执行出错: ", e);
                sseEmitter.completeWithError(e);
                currentState = AgentState.ERROR;
            } finally {
                cleanup();
            }
        });

        sseEmitter.onTimeout(() -> {
            log.warn("SSE connection timeout");
            sseEmitter.complete();
            this.cleanup();
        });

        sseEmitter.onCompletion(() -> {
            if (currentState == AgentState.RUNNING) {
                currentState = AgentState.FINISHED;
            }
            log.info("SSE output successfully");
            sseEmitter.complete();
            this.cleanup();
        });

        return sseEmitter;
    }

    private boolean isStuck() {
        if (messageList.size() < 2) {
            return false;
        }

        Message lastMessage = CollUtil.getLast(messageList);
        if (StrUtil.isBlank(lastMessage.getText())) {
            return false;
        }

        int executionCount = 1;
        for (Message message: messageList.reversed()) {
            executionCount += message.getMessageType() == MessageType.ASSISTANT
                    && message.getText().equals(lastMessage.getText()) ? 1 : 0;
        }
        return executionCount >= duplicateExecutionThreshold;
    }

    protected void handleStuck() {
        String nextStepPromptWhenStuck =
                "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted.";
        nextStepPrompt = nextStepPromptWhenStuck + nextStepPrompt;
        log.warn("{} 执行遇到了阻塞，添加提示：{} ", name, nextStepPromptWhenStuck);
    }

    public abstract String step();

    protected void cleanup(){}
}
