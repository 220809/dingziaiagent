package com.dingzk.dingziaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 文件对话记忆持久化
 */
@Slf4j
public class KryoFileChatMemory implements ChatMemory {
    private final File BASE_DIR;

    private static final Kryo kryo = new Kryo();

    private static final int DEFAULT_MAX_MESSAGES = 20;

    private int maxMessages;

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(AssistantMessage.class);
    }

    public KryoFileChatMemory(String filePath, int maxMessages) {
        this.BASE_DIR = new File(filePath);
        if (!BASE_DIR.exists())
        {
            BASE_DIR.mkdirs();
        }
        this.maxMessages = maxMessages;
    }

    public KryoFileChatMemory(String filePath) {
        this(filePath, DEFAULT_MAX_MESSAGES);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> memoryMessages = getOrCreateMemoryFile(conversationId);
        storeMemoryFile(conversationId, process(memoryMessages, messages));
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateMemoryFile(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        File memoryFile = getMemoryFile(conversationId);
        if (memoryFile.exists()) {
            memoryFile.delete();
        }
    }

    /**
     * 获取本地对话记忆文件
     * @param chatId 对话 id
     * @return File
     */
    private File getMemoryFile(String chatId) {
        return new File(BASE_DIR.toString(), chatId + ".kryo");
    }

    /**
     * 读取本地对话记忆文件
     * @param chatId 对话 id
     * @return 消息列表
     */
    private List<Message> getOrCreateMemoryFile(String chatId) {
        File memoryFile = getMemoryFile(chatId);
        List<Message> messages = new ArrayList<>();

        if (memoryFile.exists()) {
            try (Input input = new Input(new FileInputStream(memoryFile))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                log.error("JsonFileChatMemory.getOrCreateMemoryFile error, message: {}", e.getCause().getMessage());
            }
        }

        return messages;
    }

    /**
     * 保存对话记忆到文件
     * @param chatId 对话id
     * @param messages 消息
     */
    private void storeMemoryFile(String chatId, List<Message> messages) {
        File memoryFile = getMemoryFile(chatId);

        try (Output output = new Output(new FileOutputStream(memoryFile))){
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            log.error("JsonFileChatMemory.storeMemoryFile error, message: {}", e.getCause().getMessage());
        }
    }

    /**
     * 维护对话记忆持久化条数
     * @param memoryMessages 保存的对话消息
     * @param newMessages 新添加对话消息
     * @return 处理后的消息
     */
    private List<Message> process(List<Message> memoryMessages, List<Message> newMessages) {
        List<Message> processedMessages = new ArrayList<>();

        Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
        boolean hasNewSystemMessage = newMessages.stream()
                .filter(SystemMessage.class::isInstance)
                .anyMatch(message -> !memoryMessagesSet.contains(message));

        memoryMessages.stream()
                .filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
                .forEach(processedMessages::add);

        processedMessages.addAll(newMessages);

        if (processedMessages.size() <= this.maxMessages) {
            return processedMessages;
        }

        int messagesToRemove = processedMessages.size() - this.maxMessages;

        List<Message> trimmedMessages = new ArrayList<>();
        int removed = 0;
        for (Message message : processedMessages) {
            if (message instanceof SystemMessage || removed >= messagesToRemove) {
                trimmedMessages.add(message);
            }
            else {
                removed++;
            }
        }

        return trimmedMessages;
    }
}