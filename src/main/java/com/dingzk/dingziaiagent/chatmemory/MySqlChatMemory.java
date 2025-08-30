package com.dingzk.dingziaiagent.chatmemory;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.dingziaiagent.converter.MessageConverter;
import com.dingzk.dingziaiagent.mapper.ChatMessageMapper;
import com.dingzk.dingziaiagent.model.ChatMessage;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class MySqlChatMemory implements ChatMemory {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private MessageConverter messageConverter;
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessages = messages.stream()
                .map(message -> messageConverter.convertToChatMessage(message, conversationId))
                .toList();

        chatMessageMapper.insert(chatMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(query);

        List<Message> messages = chatMessages.stream().map(messageConverter::convertToMessage).toList();
        return messages;
    }

    @Override
    public void clear(String conversationId) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq("conversation_id", conversationId);
        chatMessageMapper.delete(query);
    }
}
