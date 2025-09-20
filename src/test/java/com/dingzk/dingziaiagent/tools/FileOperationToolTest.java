package com.dingzk.dingziaiagent.tools;

import com.dingzk.dingziaiagent.constants.FileConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FileOperationToolTest {

    private static final String TEST_FILE_PATH = FileConstants.FILE_BASE_DIR + "/test.txt";

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String result = tool.executeReadFile(TEST_FILE_PATH);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String result = tool.executeWriteFile(TEST_FILE_PATH, "这是一个测试文件");
        Assertions.assertNotNull(result);
    }
}