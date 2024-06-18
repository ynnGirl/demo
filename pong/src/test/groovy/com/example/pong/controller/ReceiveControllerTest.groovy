package com.example.pong.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Flux
import spock.lang.Specification



@SpringBootTest
class ReceiveControllerTest extends Specification {

    @Autowired
    private ReceiveController receiveController;


    def "test receive"() {
        given:
        Flux<String> message = Flux.just(value)

        when:
        Flux<ResponseEntity<String>> result = receiveController.receive(message)

        then:
        List<ResponseEntity<String>> response=result.collectList().block()
        response == expected

        where:
        value | expected
        "hello"      | [ResponseEntity.ok("world")]

    }



}