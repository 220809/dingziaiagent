package com.dingzk.dingziaiagent.tools;

import com.google.gson.*;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebSearchTool {

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "This is a tool which can search information from web using Baidu")
    public String executeWebSearch(@ToolParam(description = "Keyword used in searching") String query) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.get("https://www.searchapi.io/api/v1/search").newBuilder();
        urlBuilder.addQueryParameter("engine", "baidu");
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("api_key", apiKey);

        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .build();

        try (Response response = client.newCall(request).execute()) {
            String result = response.body().string();
            // 仅解析出搜索结果网页地址
            JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
            JsonArray webResults = jsonObject.getAsJsonArray("organic_results");
            List<WebSearchResult> webSearchResults = webResults.asList().stream().map(res -> {
                String title = res.getAsJsonObject().get("title").getAsString();
                String link = res.getAsJsonObject().get("link").getAsString();
                WebSearchResult searchResult = new WebSearchResult(title, link);
                return searchResult;
            }).toList();
            return webSearchResults.stream().map(WebSearchResult::toString).collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Error occurred when searching from web: " + e.getMessage();
        }
    }

    record WebSearchResult(String title, String link) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WebSearchResult that = (WebSearchResult) o;
            return Objects.equals(title, that.title) && Objects.equals(link, that.link);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, link);
        }

        @Override
        public String toString() {
            return "{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }
    }
}
