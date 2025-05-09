package com.yoger.productserviceorganization.product.application.port.in;

import com.yoger.productserviceorganization.product.application.port.in.command.RegisterProductCommand;
import com.yoger.productserviceorganization.product.domain.model.Product;

public interface RegisterProductUseCase {
    Product register(RegisterProductCommand command);
}
