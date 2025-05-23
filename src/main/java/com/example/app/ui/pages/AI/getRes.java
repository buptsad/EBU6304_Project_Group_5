package com.example.app.ui.pages.AI;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class getRes {
    /**
     * 请求API地址
     */
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    /**
     * 你在DeepSeek官网申请的API KEY，注意不要泄露给他人！
     */
    private static String API_KEY = "sk-fdf26a37926f46ab8d4884c2cd533db8";
    private final RestTemplate restTemplate = new RestTemplate();

    public String getResponse(String apiKey, String prompt) throws IOException {
        // 构建请求体
        DeepseekRequest.Message message = DeepseekRequest.Message.builder()
                .role("user")
                .content(prompt)
                .build();
        DeepseekRequest requestBody = DeepseekRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(message))
                .build();

        // 将请求体序列化为 JSON 字符串
        String jsonBody = new ObjectMapper().writeValueAsString(requestBody);

        // 创建 HTTP 请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // 等效于 headers.add("Authorization", "Bearer " + apiKey)

        // 构建 HttpEntity 对象
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

        // 发送 HTTP POST 请求并获取响应
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_URL, httpEntity, String.class);

        // 判断响应状态码和响应体是否有效
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        }
        throw new IOException("Unexpected status code" );
    }
        public String parseAIResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray choices = root.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject first = choices.getJSONObject(0);
                JSONObject message = first.getJSONObject("message");
                return message.getString("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error: Unable to parse AI response.";
    }
    public static void main(String[] args) {

    }
}
