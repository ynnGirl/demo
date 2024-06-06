package com.example.ping;

import com.example.ping.service.MessageSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ynn
 * @date 2024/6/4
 */
@SpringBootApplication
public class PingApplication {
    private static final Logger logger = LoggerFactory.getLogger(PingApplication.class);
    @Autowired
    private MessageSendService messageSendService;
    private static final String LOCK_FILE_NAME = "ping.lock";
    private static final int MAX_CONCURRENT_REQUESTS = 2;
    private final AtomicInteger countReq=new AtomicInteger();
    public static void main(String[] args) {
        SpringApplication.run(PingApplication.class, args);
    }

    @PostConstruct
    public void initMethod() {
        try(RandomAccessFile file =new RandomAccessFile(LOCK_FILE_NAME, "rw");
            FileChannel channel =file.getChannel(); FileLock fileLock =channel.tryLock()){
            countReq.set(readCounter(file));
            if (fileLock != null) {
                logger.info("countReq===={}MAX_CONCURRENT_REQUESTS={}",countReq,MAX_CONCURRENT_REQUESTS);
                if (countReq.incrementAndGet() <= MAX_CONCURRENT_REQUESTS) {
                    updateCounter(file,countReq.get());
                    messageSendService.pushMessages();
                } else {
                    logger.info("rate limited,countReq===={}",countReq);
                    throw new IOException("文件已被其他进程锁定");
                }
            }else {
                throw new IOException("文件锁获取异常");
            }
        }catch (Exception e){
            logger.error("超出请求进程数");
        }

    }
    private int readCounter(RandomAccessFile lockFile)  throws IOException{
        String line = lockFile.readLine();
        if (line != null) {
            int count=Integer.parseInt(line.trim());
            logger.info("reader count===={}",count);
            if(count>MAX_CONCURRENT_REQUESTS){
                logger.info("读取文件进程数超过2");
            }
            return count;
        }else {
            return 0;
        }
    }

    private void updateCounter(RandomAccessFile lockFile,int counter)  throws IOException {
        logger.info("写入文件值"+counter);
        lockFile.setLength(0);
        lockFile.writeBytes(String.valueOf(counter));
    }

    @PreDestroy
    public void cleanUpMethod() {
        try (RandomAccessFile file = new RandomAccessFile(LOCK_FILE_NAME, "rw");
             FileChannel channel = file.getChannel();
             FileLock fileLock = channel.tryLock()) {
            if (fileLock != null) {
                int count = readCounter(file) - 1; // 减去1
                updateCounter(file, count);
            } else {
                logger.info("文件已被其他进程锁定");
                throw new IOException("文件已被其他进程锁定");
            }
        } catch (Exception e) {
            logger.error("释放文件锁异常");
        }
    }
}
