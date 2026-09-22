package com.cafeadmin.common.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void reusesIncomingCorrelationIdAndEchoesIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        request.addHeader(CorrelationId.HEADER, "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, capturing(seen));

        assertThat(seen.get()).isEqualTo("trace-123");
        assertThat(response.getHeader(CorrelationId.HEADER)).isEqualTo("trace-123");
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        filter.doFilter(request, response, capturing(seen));

        assertThat(seen.get()).isNotBlank();
        assertThat(response.getHeader(CorrelationId.HEADER)).isEqualTo(seen.get());
    }

    private FilterChain capturing(AtomicReference<String> seen) {
        return (req, res) -> seen.set(MDC.get(CorrelationId.MDC_KEY));
    }
}
