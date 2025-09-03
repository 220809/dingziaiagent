package com.dingzk.dingziaiagent.converter;

import com.dingzk.dingziaiagent.model.ChatMessage;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.messages.*;

/**
 * Message、ChatMessage 转换类
 */
public class MessageConverter {
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    /**
     * Message 转换为 ChatMessage
     * @param message Message
     * @param conversationId 对话 id
     * @return ChatMessage
     */
    public static ChatMessage convertToChatMessage(Message message, String conversationId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(message.getMessageType().ordinal());
        chatMessage.setConversationId(conversationId);
        chatMessage.setId(((Integer) message.getMetadata().get("chatMessageId")));
        try (Output output = new Output(1024)) {
            kryo.writeObject(output, message);

            chatMessage.setMessageContent(output.toBytes());
        }
        return chatMessage;
    }

    /**
     * ChatMessage 转换为 Message
     * @param chatMessage ChatMessage
     * @return Message
     */
    public static Message convertToMessage(ChatMessage chatMessage) {
        Class<? extends Message> messageClass = switch (chatMessage.getMessageType()) {
            case 0 -> UserMessage.class;
            case 1 -> AssistantMessage.class;
            case 2 -> SystemMessage.class;
            case 3 -> ToolResponseMessage.class;
            default -> throw new IllegalStateException("Unexpected value: " + chatMessage.getMessageType());
        };
        Message message = null;
        try (Input input = new Input(chatMessage.getMessageContent())) {
            message = kryo.readObject(input, messageClass);
            message.getMetadata().put("chatMessageId", chatMessage.getId());
        }
        return message;
    }
}
