
import com.example.ping.service.MessageSendService
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.logging.Logger


@ExtendWith(SpringExtension.class)
@WebFluxTest(MessageSendServiceImpl.class)
class MessageSendServiceImpl extends Specification {

    @Autowired
    private MessageSendService messageSendService;
    @MockBean
    private WebClient.Builder webClientBuilder;
    @MockBean(answer = Answers.RETURNS_DEEP_STUBS)
    private Logger loggerMock;
    private WebClient webClient;
    def setup() {
        // 使用 MockBean 的 WebClient.Builder 来创建一个模拟的 WebClient
        webClient = webClientBuilder.build();
    }

    def "Test pushMessages method with success request"() {
        given:
        webClient.get().uri("localhost:8088/receive").retrieve() >> Flux.just("world")

        when:
        messageSendService.pushMessages()

        then:
        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("接收到pong服务的响应消息为world"), Mockito.anyString())

    }

    def "Test pushMessages method with limitRe request"() {
        given:
        webClient.get().uri("localhost:8088/receive").retrieve() >> Flux.just("429")

        when:
        messageSendService.pushMessages()

        then:
        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("too many request"), Mockito.anyString())

    }

    def "Test pushMessages method with errorRe request"() {
        given:
        webClient.get().uri("localhost:8088/receive").retrieve() >> Flux.error(new RuntimeException("Request failed"))

        when:
        messageSendService.pushMessages()

        then:
        Mockito.verify(loggerMock, Mockito.atLeastOnce()).info(Mockito.eq("t请求错误"), Mockito.anyString())

    }


}