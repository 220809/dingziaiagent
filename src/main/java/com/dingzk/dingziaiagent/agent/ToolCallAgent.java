package com.dingzk.dingziaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.dingzk.dingziaiagent.agent.state.AgentState;
import com.dingzk.dingziaiagent.rag.LizhiToolRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class ToolCallAgent extends ReActAgent{

    // 可调用工具
    private List<ToolCallback> toolCallbacks;

    private ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    private ChatResponse toolCallbackChatResponse;

    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(toolCallbacks)
                    .call()
                    .chatResponse();
            toolCallbackChatResponse = chatResponse;
            // AI 回复
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info("{} 的思考结果: {}", getName(), result);
            if (StrUtil.isNotBlank(result)) {
                sseEmitter.send(String.format("%s 给出的结果: %s", getName(), result));
            }
            log.info("{} 选择了 {} 个工具调用", getName(), toolCalls.size());
            for (AssistantMessage.ToolCall toolCall : toolCalls) {
                sseEmitter.send(LizhiToolRegistry.getToolPrompt(toolCall));
            }

            if (toolCalls.isEmpty()) {
                // 无工具调用，记录 AI 返回消息
                getMessageList().add(assistantMessage);
                return false;
            }
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> String.format("工具名称: %s%n工具参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            return true;
        } catch (Exception e) {
            log.error("{} 思考过程发生了错误: ", getName(), e);
            getMessageList().add(new AssistantMessage("处理时发生了错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallbackChatResponse);
        // 包含 ai 选择工具、工具调用结果的 Message
        List<Message> conversationHistory = toolExecutionResult.conversationHistory();
        setMessageList(conversationHistory);
        if (CollUtil.getLast(conversationHistory) instanceof ToolResponseMessage toolResponseMessage) {
            String toolResponseInfo = toolResponseMessage.getResponses().stream()
                    .map(response -> String.format("工具: %s 调用成功, 调用结果: %s", response.name(), response.responseData()))
                    .collect(Collectors.joining("\n"));
            log.info(toolResponseInfo);
            boolean terminationToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(res -> "doTerminationTool".equals(res.name()));
            if (terminationToolCalled) {
                setCurrentState(AgentState.FINISHED);
            }
            return toolResponseInfo;
        }
        return "工具调用过程出错!";
    }

    public ToolCallAgent(List<ToolCallback> toolCallbacks) {
        this.toolCallbacks = toolCallbacks;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }
}
