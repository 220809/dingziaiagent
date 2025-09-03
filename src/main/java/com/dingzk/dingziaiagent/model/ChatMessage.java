package com.dingzk.dingziaiagent.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 对话记忆消息
 * @TableName chat_message
 */
@TableName(value ="chat_message")
@Data
public class ChatMessage {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户会话id
     */
    private String conversationId;

    /**
     * 消息类型，0-用户消息，1-助手消息，2-系统消息，3工具消息
     */
    private Integer messageType;

    /**
     * 消息序列化数据
     */
    private byte[] messageContent;

    /**
     * 消息创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 是否删除 0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}