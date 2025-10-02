package com.yoger.productserviceorganization.global.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

public final class TraceUtil {

    private TraceUtil() { }

    // Kafka 헤더 → OTEL Context 추출
    private static final TextMapGetter<Headers> KAFKA_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(final Headers carrier) {
            if (carrier == null) {
                return List.of();
            }
            final List<String> keys = new ArrayList<>();
            for (Header h : carrier) {
                keys.add(h.key());
            }
            return keys;
        }

        @Override
        public String get(final Headers carrier, final String key) {
            if (carrier == null || key == null) {
                return null;
            }
            final Header header = carrier.lastHeader(key);
            return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
        }
    };

    /* 레코드의 Kafka 헤더에서 W3C tracecontext를 추출해 Context로 복원 */
    public static Context extractFromKafkaHeaders(final Headers headers) {
        return GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), headers, KAFKA_GETTER);
    }

    /* 현재 Context를 java.util.Properties 문자열로 직렬화( traceparent 등 포함 ) */
    public static String serializedTracingProperties() {
        final Properties props = new Properties();
        GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), props, (carrier, key, value) -> {
                    if (carrier != null && key != null && value != null) {
                        carrier.setProperty(key, value);
                    }
                });
        try (StringWriter writer = new StringWriter()) {
            props.store(writer, null);
            return writer.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize tracing properties", e);
        }
    }

    public static String currentTraceparent() {
        final SpanContext sc = Span.current().getSpanContext();
        if (!sc.isValid()) {
            return null;
        }
        return String.format(
                "00-%s-%s-%02x",
                sc.getTraceId(),
                sc.getSpanId(),
                sc.getTraceFlags().asByte()
        );
    }
}
