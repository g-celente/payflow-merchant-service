package br.com.payflow.merchant.infrastructure.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import br.com.payflow.merchant.api.ApiSecurity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

// Every /v1 route except /v1/merchants/me only answers platform callers that send X-Internal-Key.
public final class InternalKeyFilter extends OncePerRequestFilter {

    private static final String PROTECTED_PREFIX = "/v1/";
    private static final String MERCHANT_ROUTE = "/v1/merchants/me";
    private static final String PROBLEM = """
            {"type":"about:blank","title":"Unauthorized","status":401,"detail":"Missing or invalid internal key"}""";

    private final byte[] expected;

    public InternalKeyFilter(String internalKey) {
        this.expected = internalKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith(PROTECTED_PREFIX) || path.equals(MERCHANT_ROUTE);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(ApiSecurity.INTERNAL_KEY_HEADER);
        // Constant-time comparison, so response timing does not reveal how much of the key matched.
        if (provided != null && MessageDigest.isEqual(expected, provided.getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "ApiKey header=\"" + ApiSecurity.INTERNAL_KEY_HEADER + "\"");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getOutputStream().write(PROBLEM.getBytes(StandardCharsets.UTF_8));
    }
}
