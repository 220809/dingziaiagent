package com.dingzk.dingziaiagent.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.dingziaiagent.converter.MessageConverter;
import com.dingzk.dingziaiagent.model.ChatMessage;
import com.esotericsoftware.kryo.Kryo;
import jakarta.annotation.Resource;
import org.apache.ibatis.executor.BatchResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.messages.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class ChatMessageMapperTest {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    private static final Kryo kryo = new Kryo();

    private static final UserMessage TEST_USER_MESSAGE = new UserMessage("hello");
    private static final SystemMessage TEST_SYSTEM_MESSAGE = new SystemMessage("hello");
    private static final AssistantMessage TEST_ASSISTANT_MESSAGE = new AssistantMessage("hello");
    private static final ToolResponseMessage TEST_TOOL_RESPONSE_MESSAGE = new ToolResponseMessage(Collections.emptyList());

    private static final String TEST_CONVERSATION_ID = "a11";

    private final List<ChatMessage> TEST_CHAT_MESSAGE_LIST = new ArrayList<>();

    @BeforeAll
    static void setup() {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    @BeforeEach
    void setTestChatMessageList() {
        TEST_CHAT_MESSAGE_LIST.clear();
        TEST_CHAT_MESSAGE_LIST.add(MessageConverter.convertToChatMessage(TEST_USER_MESSAGE, TEST_CONVERSATION_ID));
        TEST_CHAT_MESSAGE_LIST.add(MessageConverter.convertToChatMessage(TEST_ASSISTANT_MESSAGE, TEST_CONVERSATION_ID));
        TEST_CHAT_MESSAGE_LIST.add(MessageConverter.convertToChatMessage(TEST_SYSTEM_MESSAGE, TEST_CONVERSATION_ID));
        TEST_CHAT_MESSAGE_LIST.add(MessageConverter.convertToChatMessage(TEST_TOOL_RESPONSE_MESSAGE, TEST_CONVERSATION_ID));
    }

    @Test
    void testInsertMessage() {
        List<BatchResult> insert = chatMessageMapper.insert(TEST_CHAT_MESSAGE_LIST);

        assertFalse(insert.isEmpty());
    }

    @Test
    void testGetMessage() {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq("conversation_id", TEST_CONVERSATION_ID);
        List<ChatMessage> chatMessages = chatMessageMapper.selectList(query);

        List<Message> list = chatMessages.stream().map(MessageConverter::convertToMessage).toList();

        for (Message m : list) {
            switch (m.getMessageType()) {
                case USER -> assertEquals(TEST_USER_MESSAGE, m);
                case ASSISTANT -> assertEquals(TEST_ASSISTANT_MESSAGE, m);
                case SYSTEM -> assertEquals(TEST_SYSTEM_MESSAGE, m);
                case TOOL -> assertEquals(TEST_TOOL_RESPONSE_MESSAGE, m);
            }
        }
    }
}