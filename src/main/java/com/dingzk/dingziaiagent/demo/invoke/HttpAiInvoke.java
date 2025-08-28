package com.dingzk.dingziaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class HttpAiInvoke {

    private static final String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    private static final String API_KEY = TestApiKey.ApiKey; // 替换为您的实际 API Key

    public static void main(String[] args) {
        String response = callDashScopeAPI();
        System.out.println("API 响应: " + response);
    }

    public static String callDashScopeAPI() {
        // 构建请求 JSON 数据
        JSONObject requestData = new JSONObject();
        requestData.put("model", "qwen-plus");

        JSONObject input = new JSONObject();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "你是谁？");

        input.put("messages", JSONUtil.createArray().put(systemMessage).put(userMessage));
        requestData.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        requestData.put("parameters", parameters);

        try {
            // 发送 POST 请求
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(requestData.toString())
                    .timeout(30000) // 30秒超时
                    .execute();

            if (response.isOk()) {
                return response.body();
            } else {
                return "请求失败，状态码: " + response.getStatus() + ", 响应: " + response.body();
            }
        } catch (Exception e) {
            return "请求异常: " + e.getMessage();
        }
    }
}