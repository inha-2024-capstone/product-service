package com.yoger.productserviceorganization.product.application;

import com.yoger.productserviceorganization.product.adapters.web.dto.request.ExpiredProductIdsDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.orderCountResponseDTOs;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.partialRefundRequestDTO;
import com.yoger.productserviceorganization.product.adapters.web.dto.response.partialRefundRequestDTOs;
import com.yoger.productserviceorganization.product.domain.model.Product;
import com.yoger.productserviceorganization.product.domain.model.ProductState;
import com.yoger.productserviceorganization.product.application.port.ProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class DueDateService {
    private final ProductRepository productRepository;
    private final RestClient orderServiceRestClient;
    private final ProductService productService;

    @Scheduled(cron = "0 0 * * * ?")
    public void scheduleDueDate() {
        List<Product> products = productRepository.findByState(ProductState.SELLABLE);
        List<Long> saleEndedProductIds = products.stream()
                .filter(product -> product.getDueDate().isBefore(LocalDateTime.now()))
                .mapToLong(Product::getId)
                .boxed().toList();
        if (saleEndedProductIds.isEmpty()) {
            return;
        }
        orderCountResponseDTOs orderCountResponseDTOs = orderServiceRestClient
                .post()
                .uri("/api/orders/products/count")
                .body(new ExpiredProductIdsDTO(saleEndedProductIds))
                .retrieve()
                .body(orderCountResponseDTOs.class);
        List<partialRefundRequestDTO> partialRefundRequestDTO = orderCountResponseDTOs.orderCountResponseDTOs()
                .stream()
                .map(val -> productService.updateSellableToSaleEnded(val.productId(), val.count()))
                .toList();
        orderServiceRestClient
                .post()
                .uri("/api/orders/products/expire")
                .body(new partialRefundRequestDTOs(partialRefundRequestDTO))
                .retrieve();
    }
}
