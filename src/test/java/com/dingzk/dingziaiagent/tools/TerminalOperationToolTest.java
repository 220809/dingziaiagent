package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TerminalOperationToolTest {

    @Test
    void executeTerminalOperation() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String command = "cmd.exe /c dir";
        String result = tool.executeTerminalOperation(command);
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}