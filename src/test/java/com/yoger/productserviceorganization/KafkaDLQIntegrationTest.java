package com.yoger.productserviceorganization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.yoger.productserviceorganization.config.RedisTestConfig;
import com.yoger.productserviceorganization.product.application.port.out.ManageProductImagePort;
import com.yoger.productserviceorganization.review.application.port.out.ReviewImageStorage;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = RedisTestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaDLQIntegrationTest {
    private static final String ORIGIN_TOPIC = "yoger.order.prd.created";
    private static final String DLQ_TOPIC    = ORIGIN_TOPIC + ".DLT";

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.2.1"));

    static { kafka.start(); }

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry r) {
        r.add("spring.kafka.producer.bootstrap-servers", kafka::getBootstrapServers);
        r.add("spring.kafka.consumer.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /* ❗ 의존성 Mock */
    @MockBean ManageProductImagePort manageProductImagePort;
    @MockBean ReviewImageStorage reviewImageStorage;

    /* DLQ 메시지 확인용 큐 */
    private static final BlockingQueue<String> DLQ_Q = new LinkedBlockingQueue<>();

    @BeforeEach void clear() { DLQ_Q.clear(); }

    @Test
    void 역직렬화_실패_메시지가_DLQ로_간다() throws Exception {
        // OrderCreatedEvent 스키마에 맞지 않는 JSON → 역직렬화 실패 유도
        String badJson = "{\"invalid\":\"data without required fields\"}";

        kafkaTemplate.send(ORIGIN_TOPIC, badJson);
        kafkaTemplate.flush();

        String dlqMsg = DLQ_Q.poll(10, TimeUnit.SECONDS);

        assertThat(dlqMsg).isNotNull();
        assertThat(dlqMsg).contains("invalid");
    }

    /* 테스트용 DLQ 리스너 */
    @KafkaListener(topics = DLQ_TOPIC, groupId = "dlt-test-consumer")
    void listenFromDLQ(String msg) { DLQ_Q.add(msg); }
}
