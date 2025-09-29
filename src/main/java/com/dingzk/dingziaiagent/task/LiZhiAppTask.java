package com.dingzk.dingziaiagent.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

import static com.dingzk.dingziaiagent.constants.FileConstants.CHAT_MEMORY_FILE_DIR;

@Component
@Slf4j
public class LiZhiAppTask {

//    @Resource
//    private ChatMessageMapper chatMessageMapper;

    /**
     * 每3天清除一次数据库 ChatMessage
     */
//    @Scheduled(cron = "* * * */3 * ?")
//    private void fullDeleteChatMessage() {
//        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
//        query.eq("deleted", 1);
//        chatMessageMapper.delete(query);
//    }

    /**
     * 每天清理一次对话记忆
     */
    @Scheduled(cron = "* * * */1 * ?")
    private void deleteChatFilePerDay() {
        final long deleteInterval = 1000 * 60 * 60 * 24 * 3;  // 3 天

        File dir = new File(CHAT_MEMORY_FILE_DIR);
        if (!dir.exists()) {
            log.warn("Chat memory directory does not exist");
            return;
        }
        for (File file : dir.listFiles()) {
            Date current = new Date();
            long interval = current.getTime() - file.lastModified();
            if (interval > deleteInterval) {
                boolean result = file.delete();
                if (!result) {
                    log.warn("Delete chat memory file failed");
                }
            }
        }
    }
}
