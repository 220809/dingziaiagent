package com.dingzk.dingziaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.dingzk.dingziaiagent.constants.FileConstants;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class WebDownloadTool {

    @Tool(description = "This is a tool which can used to download resource from internet")
    public String executeWebDownload(@ToolParam(description = "The url of downloaded resource") String url,
                                     @ToolParam(description = "The file name of the locally stored resource") String fileName) {
        File destDir = new File(FileConstants.FILE_BASE_DIR, "download");

        try {
            FileUtil.mkdir(destDir);
            // 一行代码完成下载
            HttpUtil.downloadFile(url, FileUtil.file(destDir, fileName));
            return "Resource download success!";
        } catch (Exception e) {
            return "Error occurred when downloading resource: " + e.getMessage();
        }
    }
}
