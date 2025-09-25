package com.dingzk.dingziaiagent.agent.state;

import lombok.AllArgsConstructor;
@AllArgsConstructor
public enum AgentState {
    IDLE("空闲"),
    RUNNING("运行"),
    FINISHED("完成"),
    ERROR("错误");

    public final String description;
}
