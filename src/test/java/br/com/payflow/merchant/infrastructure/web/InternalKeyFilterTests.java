package br.com.payflow.merchant.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class InternalKeyFilterTests {

    private static final String KEY = "internal-key-for-tests-0123456789abcdef";

    private final InternalKeyFilter filter = new InternalKeyFilter(KEY);

    private MockFilterChain run(String path, String internalKey, MockHttpServletResponse response) throws Exception {
        var request = new MockHttpServletRequest("GET", path);
        if (internalKey != null) {
            request.addHeader("X-Internal-Key", internalKey);
        }
        var chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return chain;
    }

    @Test
    void protectedRouteWithTheKeyPasses() throws Exception {
        var response = new MockHttpServletResponse();
        assertThat(run("/v1/merchants", KEY, response).getRequest()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void protectedRouteWithoutOrWithWrongKeyIsUnauthorized() throws Exception {
        for (String key : new String[]{null, "", "wrong", KEY + "x", KEY.substring(1)}) {
            var response = new MockHttpServletResponse();
            assertThat(run("/v1/merchants/123/webhook", key, response).getRequest()).isNull();
            assertThat(response.getStatus()).isEqualTo(401);
            assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("ApiKey header=\"X-Internal-Key\"");
            assertThat(response.getContentType()).isEqualTo("application/problem+json");
            assertThat(response.getContentAsString()).contains("\"status\":401");
        }
    }

    @Test
    void merchantRouteHealthAndSwaggerSkipTheFilter() throws Exception {
        for (String path : new String[]{"/v1/merchants/me", "/actuator/health", "/swagger-ui.html", "/v3/api-docs"}) {
            var response = new MockHttpServletResponse();
            assertThat(run(path, null, response).getRequest()).as(path).isNotNull();
        }
    }
}
