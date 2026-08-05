package dev.altencir.ecommerce.shared;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public final class WebSupport {
    private WebSupport() {}
    public static class NotFound extends RuntimeException { public NotFound(String message) { super(message); } }
    public static class Conflict extends RuntimeException { public Conflict(String message) { super(message); } }
    public static class Forbidden extends RuntimeException { public Forbidden(String message) { super(message); } }
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(WebSupport.NotFound.class) ProblemDetail notFound(WebSupport.NotFound e) { return problem(HttpStatus.NOT_FOUND, "Resource not found", e); }
    @ExceptionHandler(WebSupport.Conflict.class) ProblemDetail conflict(WebSupport.Conflict e) { return problem(HttpStatus.CONFLICT, "Conflict", e); }
    @ExceptionHandler(WebSupport.Forbidden.class) ProblemDetail forbidden(WebSupport.Forbidden e) { return problem(HttpStatus.FORBIDDEN, "Forbidden", e); }
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class}) ProblemDetail badRequest(Exception e) { return problem(HttpStatus.BAD_REQUEST, "Invalid request", e); }
    private ProblemDetail problem(HttpStatus status, String title, Exception e) {
        var detail = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        detail.setTitle(title); detail.setType(URI.create("https://ecommerce.local/problems/" + status.value()));
        return detail;
    }
}

@Component
class CorrelationIdFilter implements Filter {
    static final String HEADER = "X-Correlation-ID";
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        var http = (HttpServletRequest) request;
        var id = http.getHeader(HEADER);
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        ((HttpServletResponse) response).setHeader(HEADER, id);
        try (var ignored = MDC.putCloseable("correlationId", id)) { chain.doFilter(request, response); }
    }
}
