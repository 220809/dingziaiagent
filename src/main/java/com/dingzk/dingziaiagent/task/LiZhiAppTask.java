package com.dingzk.dingziaiagent.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingzk.dingziaiagent.mapper.ChatMessageMapper;
import com.dingzk.dingziaiagent.model.ChatMessage;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LiZhiAppTask {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    /**
     * 每3天清除一次数据库 ChatMessage
     */
    @Scheduled(cron = "* * * */3 * ?")
    private void fullDeleteChatMessage() {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.eq("deleted", 1);
        chatMessageMapper.delete(query);
    }
}
