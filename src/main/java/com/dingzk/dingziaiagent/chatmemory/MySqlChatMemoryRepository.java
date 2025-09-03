package com.dingzk.dingziaiagent.chatmemory;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.dingziaiagent.converter.MessageConverter;
import com.dingzk.dingziaiagent.mapper.ChatMessageMapper;
import com.dingzk.dingziaiagent.model.ChatMessage;
import lombok.Builder;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用 SpringAI 1.0.0 标准实现 MySQL 数据库保存对话记忆
 */
@Builder
public class MySqlChatMemoryRepository implements ChatMemoryRepository {
    private ChatMessageMapper chatMessageMapper;

    private static final String CONVERSATION_ID_KEY = "conversation_id";


    @Override
    public List<String> findConversationIds() {
        return chatMessageMapper.findConversationIds();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<ChatMessage> chatMessages = findChatMessagesByConversationId(conversationId);
        return chatMessages.stream()
                .map(MessageConverter::convertToMessage)
                .toList();
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessagesToSave = messages.stream()
                .map(message -> MessageConverter.convertToChatMessage(message, conversationId))
                .toList();
        List<ChatMessage> storedChatMessages = findChatMessagesByConversationId(conversationId);
        Set<Integer> chatMessageIdSet = chatMessagesToSave.stream()
                .map(ChatMessage::getId)
                .collect(Collectors.toSet());

        // 需要删除 ChatMessage
        List<ChatMessage> chatMessageToDelete = storedChatMessages.stream()
                .filter(chatMessage -> !chatMessageIdSet.contains(chatMessage.getId()))
                .toList();

        if (CollectionUtil.isNotEmpty(chatMessageToDelete)) {
            chatMessageMapper.deleteByIds(chatMessageToDelete);
        }

        chatMessageMapper.insertOrUpdate(chatMessagesToSave);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq(CONVERSATION_ID_KEY, conversationId);
        chatMessageMapper.delete(query);
    }

    private List<ChatMessage> findChatMessagesByConversationId(String conversationId) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq(CONVERSATION_ID_KEY, conversationId);
        return chatMessageMapper.selectList(query);
    }
}
