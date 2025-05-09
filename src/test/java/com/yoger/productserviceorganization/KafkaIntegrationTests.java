package com.yoger.productserviceorganization;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.awaitility.Awaitility.await;

import com.yoger.productserviceorganization.config.RedisTestConfig;
import com.yoger.productserviceorganization.product.adapters.messaging.kafka.consumer.event.OrderCreatedEvent;
import com.yoger.productserviceorganization.product.application.port.in.DeductStockUseCase;
import com.yoger.productserviceorganization.product.application.port.out.LoadProductPort;
import com.yoger.productserviceorganization.product.application.port.out.ManageProductImagePort;
import com.yoger.productserviceorganization.product.application.port.out.PersistProductPort;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.review.application.port.out.ReviewImageStorage;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
        classes = RedisTestConfig.class
)
@ActiveProfiles("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaIntegrationTests {
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.2.1")
    );

    static {
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.producer.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry registry;

    @Autowired
    DeductStockUseCase deductStockUseCase;

    @Autowired
    LoadProductPort loadProductPort;

    @Autowired
    PersistProductPort persistProductPort;

    @MockBean
    ManageProductImagePort manageProductImagePort;

    @MockBean
    ReviewImageStorage reviewImageStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    PlatformTransactionManager transactionManager;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE product_jpa_entity");
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @BeforeEach
    public void setUp() {
        Product product = Product.of(
                1L,
                "test",
                1000,
                "this is test",
                "https://my-bucket.s3.us-west-1.amazonaws.com/myimage.jpg",
                "https://my-bucket.s3.us-west-1.amazonaws.com/my-thumbnail.jpg",
                ProductState.SELLABLE,
                1L, // creatorId
                "제작자 이름1", // creatorName
                LocalDateTime.now().plusDays(30), // dueDate
                100 // stockQuantity
        );
        persistProductPort.persist(product);
    }

    @Test
    void testOrderCreatedEventConsumer() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                999L,
                "OrderCreated",
                new OrderCreatedEvent.OrderCreatedEventData(100L, 1L, 1),
                LocalDateTime.now()
        );

        // Awaitility로 최대 10초 기다리며 재시도
        kafkaTemplate.send("yoger.order.prd.created", event);
        kafkaTemplate.flush();

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                Product product = loadProductPort.loadProductWithLock(1L);
                assertThat(product.getStockQuantity()).isEqualTo(99);
            });
        });
    }

    @Test
    void testDuplicateOrderCreatedEventConsume() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                999L,
                "OrderCreated",
                new OrderCreatedEvent.OrderCreatedEventData(100L, 1L, 1),
                LocalDateTime.now()
        );

        kafkaTemplate.send("yoger.order.prd.created", event);
        kafkaTemplate.flush();

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                Product product = loadProductPort.loadProductWithLock(1L);
                assertThat(product.getStockQuantity()).isEqualTo(99);
            });
        });

        Thread.sleep(1000);

        kafkaTemplate.send("yoger.order.prd.created", event);
        kafkaTemplate.flush();

        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.executeWithoutResult(status -> {
                Product product = loadProductPort.loadProductWithLock(1L);
                assertThat(product.getStockQuantity()).isEqualTo(99);
            });
        });
    }
}
