package com.yoger.productserviceorganization.product.application.port.service;

import com.yoger.productserviceorganization.product.application.port.out.ClearOutboxEventPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxCleanupService {
    private final ClearOutboxEventPort clearOutboxEventPort;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanOutboxTable() {
        clearOutboxEventPort.clear();
    }
}
