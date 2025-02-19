package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.application.port.out.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxCleanupService {
    private final OutboxRepository outboxRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanOutboxTable() {
        outboxRepository.deleteAll();
    }
}
