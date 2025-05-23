package com.example.app.ui.pages.AI;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class classification {
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
        
        prompt = "请你根据以下的账单信息，将这些交易中的每一笔交易归于{Gift,Entertainment,Service,Shopping,Other,Food}中的一类。示例输入如下：2025-04-14,风味餐厅,商户消费,-15.00\r\n2025-04-14,微信转账,红包,12.00\r\n\r\n示例输出字符串如下，类别中间以逗号隔开：Food,Other，如果不属于任何类别归类为Other.除此之外不允许包含其它任何的内容" + prompt;
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
        String question = "2025-04-14,商户消费,商户消费,-15.00,false";
        try {
            String response = new classification().getResponse(API_KEY, question);
            response = new classification().parseAIResponse(response);
            System.out.println(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
