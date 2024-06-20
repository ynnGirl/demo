package com.example.ping.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import javax.annotation.PostConstruct;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.Duration;
import java.util.Objects;

/**
 * @author ynn
 * @date 2024/6/4
 */
@Component
public class MessageSendServer {
    private static final Logger logger = LoggerFactory.getLogger(MessageSendServer.class);
    @Autowired
    private WebClient webClient;
    private static final String LOCK_FILE_NAME = "ping.lock";
    private static final int MAX_CONCURRENT_REQUESTS = 2;

    public void pushMessages() {

        Flux.interval(Duration.ofSeconds(1)) // 每秒生成一个信号
                .map(tick -> "hello")
                .flatMap(t->{
                    //获取锁
                    FileLock fileLock=getLockTry();
                   logger.info("当前线程号是{}",Thread.currentThread().getName());
                   //获取锁后休眠，让其他进程获取文件锁发送成功，模拟一秒发送请求两个进程随机请求成功
                   try {
                        Thread.sleep(800);
                    }catch (Exception e){
                        logger.error(e.getMessage());
                    }
//                    try {
//                        Thread.sleep(3000);
//                    }catch (Exception e){
//                        logger.error(e.getMessage());
//                    }
//                    if (getFileLock()) {
                    if (fileLock!=null) {

                        return pongPushInit(t,fileLock);
                    }else {
                        //速率受限
                        logger.error("Speed limited");
                        return Mono.just("Request not sent");
                    }
                }) // 推送数据到消费者服务
                .subscribe(response ->{
                                logger.info("接收到pong服务的响应消息为{}",response);
                        } ,
                        error -> logger.error("请求错误"));
    }



    private Mono<String> pongPushInit(String data,FileLock fileLock) {
            return webClient.post()
                    .uri("/receive")
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(error -> { // 对请求pong服务的错误进行处理
                        return Mono.just("Error occurred while calling pong service");
                    })
                    .doFinally(unLock->{
                        try {
                            logger.info("获取文件锁为{}",fileLock);
                            fileLock.release();
                            logger.info("释放锁成功");
                        }catch (Exception e){
                            logger.error("文件释放锁异常");
                        }

                    })
                    ;
    }


    @PostConstruct
    public void initMethod() {
        pushMessages();

    }

    private FileLock getFileLimit(){
        for(int i=0;i<MAX_CONCURRENT_REQUESTS;i++){
            try{
                RandomAccessFile file = new RandomAccessFile(LOCK_FILE_NAME+i, "rw");
                FileChannel channel =file.getChannel();
                  FileLock fileLock =channel.tryLock();
                  if(fileLock!=null){
                      logger.info("获取文件锁名{}",LOCK_FILE_NAME+i);
                      return fileLock;
                  }
            }catch (Exception e){
//                logger.error("获取文件锁失败"+e.getMessage());
            }
        }
        return null;
    }


    private FileLock getLockTry(){

        FileLock fileLock =null;
        long start = System.currentTimeMillis();//系统当前时间
        try {
            while (fileLock==null) {
                fileLock=getFileLimit();
                if (!Objects.equals(fileLock, null)) {
                    return fileLock;
                } else {
                    if (System.currentTimeMillis() - start > 2000) {
                        return null;
                    }
                }
                Thread.sleep(2000);
            }
        }catch (Exception e){
            logger.error("获取文件锁失败"+e.getMessage());
        }
        return fileLock;
    }
//    private boolean getFileLock(){
//
//        try (RandomAccessFile file = new RandomAccessFile(LOCK_FILE_NAME, "rw");
//             FileChannel channel =file.getChannel()){
//            // 尝试获取文件锁
//            FileLock fileLock =null;
//            long start = System.currentTimeMillis();//系统当前时间
//            while (null==fileLock){
//                try {
//                    fileLock =channel.tryLock();
//                    if(fileLock!=null){
//                        String content = file.readLine();
//                        int now = (int) System.currentTimeMillis() / 1000;
//                        //log.info("获取锁时间"+now+"----"+content);
//                        if (StringUtils.isNullOrEmpty(content)) {
//                            file.write((now + "-1").getBytes());
//                        } else {
//                            String[] data = content.split("-");
//                            String time = data[0];
//                            String flag = data[1];
//                            String writeData;
//                            if (time.equals(Integer.toString(now))) {
//                                if (Integer.parseInt(flag) != 1) {
//                                    System.out.println("获取文件锁失败");
//                                    return false;
//                                } else {
//                                    writeData = now + "-2";
//                                }
//                            } else {
//                                writeData = now + "-1";
//                            }
//                            logger.info("---"+writeData);
//                            file.seek(0); // 将文件指针移回文件开头
//                            file.write(writeData.getBytes());
//                        }
//                    }else {
//                        if (System.currentTimeMillis() - start > 10000) {
//                            return false;
//                        }
//                    }
//                    Thread.sleep(50);
//                }catch (Exception r){
//
//                }
//            }
//
//            if (fileLock == null) {
//                System.out.println("获取文件锁失败");
//                return false;
//            }
//            // 释放锁
//            fileLock.release();
//        } catch (Exception e) {
//            logger.error("获取锁异常->{}",e.getMessage(),e);
//        }
//        return true;
//
//    }
//
}
