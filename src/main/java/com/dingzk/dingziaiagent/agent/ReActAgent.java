package com.dingzk.dingziaiagent.agent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ReActAgent extends BaseAgent{
    @Override
    public String step() {
        try {
            boolean shouldAct = think();

            if (!shouldAct) {
                return "思考结束 - 无待执行任务";
            }

            return act();
        } catch (Exception e) {
            log.info("执行出错: ", e);
            return "执行出错: " + e.getMessage();
        }
    }

    public abstract boolean think();

    public abstract String act();
}
