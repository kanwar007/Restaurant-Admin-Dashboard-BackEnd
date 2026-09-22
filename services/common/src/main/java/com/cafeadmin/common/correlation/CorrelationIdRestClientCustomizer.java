package com.cafeadmin.common.correlation;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CorrelationIdRestClientCustomizer implements RestClientCustomizer {

    @Override
    public void customize(RestClient.Builder builder) {
        builder.requestInterceptor((request, body, execution) -> {
            String correlationId = CorrelationId.current();
            if (correlationId != null) {
                request.getHeaders().set(CorrelationId.HEADER, correlationId);
            }
            return execution.execute(request, body);
        });
    }
}
