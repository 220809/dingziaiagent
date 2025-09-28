package com.dingzk.dingziaiagent.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@SpringBootTest
class JManusTest {

    @Autowired
    private JManus jManus;

    @Test
    void testJManus() {
        String message = """
                请帮我推荐3个北京海淀区适合家庭聚餐的地点，结合网络图片展示，并以 pdf 格式输出。
                """;
        SseEmitter result = jManus.run(message);
        Assertions.assertNotNull(result);
    }
}