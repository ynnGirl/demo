package com.example.ping.service.impl;

import com.example.ping.service.MessageSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ynn
 * @date 2024/6/4
 */
@Service
public class MessageSendServiceImpl implements MessageSendService {
    private static final Logger logger = LoggerFactory.getLogger(MessageSendServiceImpl.class);
    @Autowired
    private WebClient webClient;

    @Override
    public void pushMessages() {

        Flux.interval(Duration.ofSeconds(1)) // 每秒生成一个信号
                .subscribeOn(Schedulers.single())
                .map(tick -> "hello")
                .flatMap(this::pongPushInit) // 推送数据到消费者服务
                .subscribe(response ->{
                            if(Objects.equals(response,"world")){
                                logger.info("接收到pong服务的响应消息为{}",response);
                            }else if(Objects.equals(response,"429")){
                                logger.info("too many request");
                            }

                        } ,
                        error -> logger.error("请求错误"));
    }



    private Mono<String> pongPushInit(String data) {
        return webClient.post()
                .uri("/receive")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(error -> { // 对请求pong服务的错误进行处理
                    return Mono.just("Error occurred while calling pong service");

                });
    }


}
