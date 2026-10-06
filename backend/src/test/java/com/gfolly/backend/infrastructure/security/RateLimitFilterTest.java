package com.gfolly.backend.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    private final RateLimitFilter filter = new RateLimitFilter(JsonMapper.builder().build());

    @Test
    void blocksAfterMaxAttemptsForSameAccountAndEndpoint() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS; i++) {
            assertThat(call("/api/auth/login", "{\"email\":\"jane@example.com\"}").getStatus()).isEqualTo(200);
        }

        MockHttpServletResponse blocked = call("/api/auth/login", "{\"email\":\"JANE@example.com\"}");

        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getContentAsString(StandardCharsets.UTF_8)).contains("\"success\":false");
    }

    @Test
    void countersAreIsolatedPerAccountAndPerEndpoint() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS + 1; i++) {
            call("/api/auth/login", "{\"email\":\"jane@example.com\"}");
        }

        assertThat(call("/api/auth/login", "{\"email\":\"john@example.com\"}").getStatus()).isEqualTo(200);
        assertThat(call("/api/auth/forgot-password", "{\"email\":\"jane@example.com\"}").getStatus()).isEqualTo(200);
    }

    @Test
    void bodyRemainsReadableDownstream() throws Exception {
        MockHttpServletRequest request = request("/api/auth/login", "{\"email\":\"jane@example.com\"}");
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(new String(chain.getRequest().getInputStream().readAllBytes(), StandardCharsets.UTF_8))
                .isEqualTo("{\"email\":\"jane@example.com\"}");
    }

    @Test
    void nonSensitiveEndpointsAreNotLimited() throws Exception {
        for (int i = 0; i < RateLimitFilter.MAX_ATTEMPTS + 2; i++) {
            assertThat(call("/api/users", "{\"email\":\"jane@example.com\"}").getStatus()).isEqualTo(200);
        }
    }

    private MockHttpServletResponse call(String path, String body) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request(path, body), response, new MockFilterChain());
        return response;
    }

    private static MockHttpServletRequest request(String path, String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setContentType("application/json");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }
}
