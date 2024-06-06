package com.example.pong.controller;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * @author ynn
 * @date 2024/6/5
 */
@RestController
public class ReceiveController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveController.class);
    // 每秒处理一个请求
    private final RateLimiter rateLimiter = RateLimiter.create(1.0);
    // 计数器，记录当前已处理的请求个数,最大每秒一个请求处理
    private final AtomicInteger countReq = new AtomicInteger(0);

    @PostMapping(value = "/receive")
    public Flux<String> receive(@RequestBody Flux<String> message) {
        if(rateLimiter.tryAcquire()){
            if(countReq.getAndIncrement()<2 ){
                message.subscribe(
                        data -> {
                            // 处理每个数据项
                            logger.info("Received data: {}" ,data);
                            // 在这里可以执行任何业务逻辑
                        },
                        error -> {
                            logger.error("Received data: {}" ,error);
                        },
                        () -> {
                            logger.info("Data stream completed");
                        }
                );
                countReq.decrementAndGet();
                return Flux.just("world");
            }else {
                countReq.decrementAndGet();
                return Flux.just("429");
            }
        }else {
            return Flux.just("429");
        }


    }

}
