package com.example.ping.push;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author ynn
 * @date 2024/6/4
 */
@Configuration
public class WebClientConfig {
    @Value("${pong.url}") // 从配置文件中读取服务B的URL
    private String pongUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.create(pongUrl);
    }
}
