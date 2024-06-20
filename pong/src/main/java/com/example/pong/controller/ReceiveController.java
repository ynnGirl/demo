package com.example.pong.controller;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ynn
 * @date 2024/6/5
 */
@RestController
public class ReceiveController {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveController.class);
    // 每秒处理一个请求
    private final RateLimiter rateLimiter = RateLimiter.create(1.0);

    @PostMapping(value = "/receive")
    public Mono<ResponseEntity<String>> receive(@RequestBody String message) {
        if(rateLimiter.tryAcquire()){
            logger.info("收到ping服务端的消息====={}" ,message);
            //休眠使文件锁不释放，模拟最多并发两个进程
            try {
                Thread.sleep(3000);
            }catch (Exception e){
                logger.error(e.getMessage());
            }

            return Mono.just(ResponseEntity.ok().body("world"));
        }else {
            return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too Many Requests"));
        }
    }

}
