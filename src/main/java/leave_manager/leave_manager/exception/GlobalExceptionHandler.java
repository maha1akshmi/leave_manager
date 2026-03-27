package leave_manager.leave_manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- Validation errors (e.g. @NotBlank, @Email failed) ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors
        ));
    }

    // ---- Resource not found (user, leave, etc.) ----
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null
        ));
    }

    // ---- Unauthorized / forbidden access ----
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(buildError(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                null
        ));
    }

    // ---- Bad credentials on login ----
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid email or password",
                null
        ));
    }

    // ---- Insufficient leave balance ----
    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientLeaveBalanceException ex) {
        return ResponseEntity.badRequest().body(buildError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null
        ));
    }

    // ---- Duplicate email on registration ----
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null
        ));
    }

    // ---- Generic fallback ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred: " + ex.getMessage(),
                null
        ));
    }

    // ---- Helper to build consistent error response ----
    private Map<String, Object> buildError(int status, String message, Object details) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status);
        error.put("message", message);
        if (details != null) {
            error.put("errors", details);
        }
        return error;
    }
}