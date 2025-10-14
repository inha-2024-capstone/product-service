package com.yoger.productserviceorganization.product.adapters.messaging.kafka.producer.event;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    DEDUCTION_COMPLETED("deductionCompleted"),
    DEDUCTION_FAILED("deductionFailed");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
