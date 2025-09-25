package com.dingzk.dingziaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import com.dingzk.dingziaiagent.constants.FileConstants;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class FileOperationTool {

    @Tool(description = "This is a tool which can be used to read content from a file with given file path")
    public String executeReadFile(@ToolParam(description = "The file path of a file") String filePath) {
        FileReader fileReader = FileReader.create(new File(filePath), StandardCharsets.UTF_8);
        try {
            return fileReader.readString();
        } catch (Exception e) {
            return "Error occurred when reading file: " + e.getMessage();
        }
    }

    @Tool(description = "This is a tool which can be used to write content to a file with given file path")
    public String executeWriteFile(@ToolParam(description = "The file path of a file") String filePath,
                                   @ToolParam(description = "The content written to the file") String content) {
        File dir = new File(FileConstants.FILE_BASE_DIR);

        try {
            FileWriter fileWriter = FileWriter.create(new File(dir, filePath), StandardCharsets.UTF_8);
            FileUtil.mkdir(dir);
            fileWriter.write(content);
            return "Write content to file success!";
        } catch (Exception e){
            return "Error occurred when writing file: " + e.getMessage();
        }
    }
}
