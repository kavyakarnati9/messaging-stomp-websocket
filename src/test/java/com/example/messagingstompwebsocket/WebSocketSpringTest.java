package com.example.messagingstompwebsocket;

import com.example.messagingstompwebsocket.model.HelloMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketSpringTest {

    @Value("${local.server.port}")
    private int port;

    private WebSocketStompClient stompClient;

    private final String WEBSOCKET_ENDPOINT = "/websocket";

    @Before
    public void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(
                Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    public void testWebSocketEndpoint() throws Exception {
        StompSession session = stompClient
                .connect(getWsEndpoint(), new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);
        HelloMessage message = new HelloMessage("world");
        session.send("/app/hello", message);
        CountDownLatch latch = new CountDownLatch(1);
        session.subscribe("/topic/greetings", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return HelloMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                HelloMessage message = (HelloMessage) payload;
                try {
                    assertEquals("Hello, world!", message.getName());
                } finally {
                    latch.countDown();
                }
            }
        });
        if (!latch.await(3, TimeUnit.SECONDS)) {
            fail("Message not received");
        }
    }

    private String getWsEndpoint() {
        return "ws://localhost:" + port + WEBSOCKET_ENDPOINT;
    }
}
