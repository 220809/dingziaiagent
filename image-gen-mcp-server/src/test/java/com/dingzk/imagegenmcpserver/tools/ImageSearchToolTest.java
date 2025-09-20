package com.dingzk.imagegenmcpserver.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String query = "plane";
        String result = imageSearchTool.searchImage(query);
        Assertions.assertFalse(result.contains("Error occurred"));
    }
}