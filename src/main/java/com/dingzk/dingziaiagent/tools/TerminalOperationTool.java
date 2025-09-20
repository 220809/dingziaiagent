package com.dingzk.dingziaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TerminalOperationTool {

    @Tool(description = "This is a tool which can be used to execute terminal command in Windows")
    public String executeTerminalOperation(@ToolParam(description = "The command to execute") String command) {
        StringBuilder output = new StringBuilder();

        try {
            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令输出
            InputStream inputStream = process.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "GBK"); // Windows中文编码
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            output.append("\n退出代码: ").append(exitCode);

            // 关闭资源
            reader.close();
            inputStreamReader.close();
            inputStream.close();

        } catch (IOException | InterruptedException e) {
            return "Error occurred when executing command: " + e.getMessage();
        }

        return output.toString();
    }
}
