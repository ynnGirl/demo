package com.example.ping.push

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import spock.lang.Specification

import java.util.logging.Logger

@SpringBootTest
class MessageSendServerTest extends Specification {

    @Autowired
    private MessageSendServer messageSendServer
    @MockBean
    private WebClient webClient

    def setup() {
        // 创建一个模拟的 WebClient
        webClient =Mock(WebClient)
    }

    def "Test pushMessages method with success request"() {
        given:
//        webClient.get().uri("localhost:8088/receive").retrieve() >> Flux.just("world")
        def mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def mockResponseSpec = Mock(WebClient.ResponseSpec)
        def responseBody = Flux.just("world")

        webClient.get() >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.uri("localhost:8088/receive") >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.retrieve() >> mockResponseSpec
        mockResponseSpec.bodyToMono(String) >> responseBody

        when:
        messageSendServer.pushMessages()

        then:
        print("world")
//        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("接收到pong服务的响应消息为world"), Mockito.anyString())
//        def captor = ArgumentCaptor.forClass(String)
//        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(captor.capture())
//
//        // 验证传入的日志消息确实是您期望的
//        def receivedMessage = captor.getValue()
//        assert receivedMessage == "接收到pong服务的响应消息为world"
    }

    def "Test pushMessages method with limitRe request"() {
        given:
        def mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def mockResponseSpec = Mock(WebClient.ResponseSpec)
        def responseBody = Flux.just("429")

        webClient.get() >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.uri("localhost:8088/receive") >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.retrieve() >> mockResponseSpec
        mockResponseSpec.bodyToMono(String) >> responseBody
        when:
        messageSendServer.pushMessages()

        then:
        print("429")
//        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("too many request"), Mockito.anyString())

    }

    def "Test pushMessages method with errorRe request"() {
        given:
//        webClient.get().uri("localhost:8088/receive").retrieve() >> Flux.error(new RuntimeException("Request failed"))
        def mockRequestHeadersUriSpec = Mock(WebClient.RequestHeadersUriSpec)
        def mockResponseSpec = Mock(WebClient.ResponseSpec)
        def responseBody =Flux.error(new RuntimeException("Request failed"))

        webClient.get() >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.uri("localhost:8088/receive") >> mockRequestHeadersUriSpec
        mockRequestHeadersUriSpec.retrieve() >> mockResponseSpec
        mockResponseSpec.bodyToMono(String) >> responseBody
        when:
        messageSendServer.pushMessages()

        then:
        print("Request failed")
//        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("t请求错误"), Mockito.anyString())

    }


}