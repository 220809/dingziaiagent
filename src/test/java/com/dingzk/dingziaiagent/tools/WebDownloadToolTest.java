package com.dingzk.dingziaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebDownloadToolTest {

    @Test
    void executeWebDownload() {
        WebDownloadTool tool = new WebDownloadTool();
        String downloadUrl = "https://www.cainiaojc.com/static/upload/210424/0810570.png";
        String fileName = "image.png";
        String result = tool.executeWebDownload(downloadUrl, fileName);
        Assertions.assertNotNull(result);
    }
}